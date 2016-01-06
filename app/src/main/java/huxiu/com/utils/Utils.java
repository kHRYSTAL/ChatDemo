package huxiu.com.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import huxiu.com.MainApplication;


/**
 * Created by yao on 15/7/22:下午11:49.
 */
public class Utils {
    public static final int ONE_MINUTE = 60 * 1000;
    public static final int ONE_HOUR = 60 * 60 * 1000;
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int BUFFER_SIZE = 4096;

    private static final ThreadLocal<DateFormat> todayFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };

    private static final ThreadLocal<DateFormat> yesterdayFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("昨天 HH:mm");
        }
    };

    private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM-dd");
        }
    };

    private static final ThreadLocal<DateFormat> shortDateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yy-MM-dd");
        }
    };

    private static final ThreadLocal<DateFormat> shortDateFormatNoYear
            = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM-dd");
        }
    };

    private static final ThreadLocal<DateFormat> dayFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    };

    private static final ThreadLocal<DateFormat> dayFormatNoYear = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM-dd HH:mm");
        }
    };

    private static final ThreadLocal<DateFormat> datetimeFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return sdf;
        }
    };

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {

        }
    }

    public static  JSONArray buildContacts(Context context) {
        JSONArray json = new JSONArray();
        try {
            ContentResolver CR = context.getContentResolver();
            Cursor c = CR
                    .query(ContactsContract.Data.CONTENT_URI,
                            new String[]{
                                    ContactsContract.Data.MIMETYPE,
                                    ContactsContract.Data.CONTACT_ID,
                                    ContactsContract.Data.DATA1, ContactsContract.Data.DATA2,
                                    ContactsContract.Data.DATA3,
                                    ContactsContract.Data.DATA5
                            },
                            " MIMETYPE='vnd.android.cursor.item/name' OR MIMETYPE='vnd.android.cursor.item/phone_v2' OR MIMETYPE='vnd.android.cursor.item/email_v2'",
                            null, ContactsContract.Data.CONTACT_ID
                                    + ",is_super_primary desc, is_primary desc"
                    );

            if (c != null) {
                long contactId = -1;
                JSONObject people = null;
                JSONArray phone = null;
                JSONArray email = null;
                try {
                    if (c.moveToFirst()) {
                        do {
                            long id = c.getLong(1);
                            if (id != contactId) {
                                if (people != null) {
                                    try {
                                        people.put("phones", phone);
                                        people.put("emails", email);
                                    } catch (JSONException ignored) {
                                    }
                                    json.put(people);
                                }
                                people = new JSONObject();
                                phone = new JSONArray();
                                email = new JSONArray();
                                contactId = id;
                            }
                            String mimetype = c.getString(0);
                            switch (mimetype) {
                                case "vnd.android.cursor.item/name":
                                    if (people != null && !people.has("name")) {
                                        String displayName = c.getString(2);
                                        String givenName = c.getString(3);
                                        String familyName = c.getString(4);
                                        String middleName = c.getString(5);
                                        String name = formatName(displayName, familyName,
                                                middleName,
                                                givenName);
                                        try {
                                            people.put("name", name);
                                        } catch (JSONException ignored) {
                                        }
                                    }
                                    break;
                                case "vnd.android.cursor.item/phone_v2":
                                    String number = c.getString(2);
                                    if (TextUtils.isEmpty(number)) {
                                        continue;
                                    }
                                    number = number.replaceAll(" ", "");
                                    number = number.replaceAll(",", "");
                                    number = number.replaceAll("-", "");
                                    if (number.startsWith("+86")) {
                                        number = number.substring(3);
                                    }
                                    if (!TextUtils.isEmpty(number)) {
                                        phone.put(number);
                                    }
                                    break;
                                case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                                    email.put(c.getString(2));
                                    break;
                            }
                        } while (c.moveToNext());
                        if (people != null) {
                            try {
                                people.put("phones", phone);
                                people.put("emails", email);
                            } catch (JSONException ignored) {
                            }
                            json.put(people);
                        }
                    }
                } finally {
                    try {
                        c.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        } finally {

            return json;
        }
    }

    private static String formatName(String displayName, String familyName, String middleName,
                                     String givenName) {
        //TODO
        return displayName;
    }

    public static String getIMEI(Context context) {
        try {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "Unknown";
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return versionName;
    }

    public static String getDatetimeStr(long time) {
        Date date = new Date(time);
        if (isToday(date)) {
            return todayFormat.get().format(date);
        } else {
            return dateFormat.get().format(date);
        }
    }

    public static boolean isToday(Date date) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2
                .get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH) == c2
                .get(Calendar.DAY_OF_MONTH);
    }

    public static String getDateString(Date date) {
        if (date != null) {
            return dateFormat.get().format(date);
        }
        return "";
    }

    public static String getDayString(Date date) {
        if (date != null) {
            return dayFormat.get().format(date);
        }
        return "";
    }

    public static String getDayStringNoYear(Date date) {
        if (date != null) {
            return dayFormatNoYear.get().format(date);
        }
        return "";
    }

    public static String getShortDateString(Date date) {
        if (date != null) {
            return shortDateFormat.get().format(date);
        }
        return "";
    }

    public static String getThisYearShortDateString(Date date) {
        if (date != null) {
            return shortDateFormatNoYear.get().format(date);
        }
        return "";
    }

    public static Date getDate(String date) {
        if (date != null && date.trim().length() > 0) {
            try {
                return dateFormat.get().parse(date);
            } catch (ParseException e) {
            }
        }
        return new Date();
    }


    public static String getTodayString(Date date) {
        if (date != null) {
            return todayFormat.get().format(date);
        }
        return "";
    }


    public static String getDatetimeString(Date date) {
        if (date != null) {
            return datetimeFormat.get().format(date);
        }
        return "";
    }


    public static String getYesterDayString(Date date) {
        if (date != null) {
            return yesterdayFormat.get().format(date);
        }
        return "";
    }

    public static Date getDatetime(String date) {
        if (date != null) {
            try {
                return datetimeFormat.get().parse(date);
            } catch (ParseException e) {
            }
        }
        return null;
    }


    public static String getTimeShowText(Date datetime, Date lastDatetime, long interval,
                                         Date yesterday, Date today) {
        if (datetime == null) {
            return null;
        }
        if (lastDatetime != null && datetime.getTime() < lastDatetime.getTime() + interval) {
            return null;
        }
        if (yesterday != null && datetime.before(yesterday)) {
            return getDayString(datetime);
        } else if (today != null && datetime.before(today)) {
            return getYesterDayString(datetime);
        }
        return getTodayString(datetime);
    }

    public static String getTimeShowText(Date datetime, Date yesterday, Date today, Date year) {
        if (datetime == null) {
            return null;
        }
        if (yesterday != null && datetime.before(yesterday)) {
            if (datetime.after(year)) {
                return getDayStringNoYear(datetime);
            } else {
                return getDayString(datetime);
            }
        } else if (today != null && datetime.before(today)) {
            return getYesterDayString(datetime);
        }
        return getTodayString(datetime);
    }

    public static String getTimeShowText(Date datetime) {
        if (datetime == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();
        calendar.add(Calendar.DATE, -6);
        Date week = calendar.getTime();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Date year = calendar.getTime();
        if (week != null && datetime.before(week)) {
            if (datetime.after(year)) {
                return getThisYearShortDateString(datetime);
            } else {
                return getShortDateString(datetime);
            }
        } else if (yesterday != null && datetime.before(yesterday)) {
            Calendar weekDay = Calendar.getInstance();
            weekDay.setTime(datetime);
            switch (weekDay.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    return "星期一";
                case Calendar.TUESDAY:
                    return "星期二";
                case Calendar.WEDNESDAY:
                    return "星期三";
                case Calendar.THURSDAY:
                    return "星期四";
                case Calendar.FRIDAY:
                    return "星期五";
                case Calendar.SATURDAY:
                    return "星期六";
                case Calendar.SUNDAY:
                    return "星期日";
            }
        } else if (today != null && datetime.before(today)) {
            return "昨天";
        }
        return getTodayString(datetime);
    }

    public static Calendar getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Date[] getMarkDate() {
        Date today, yesterday, year;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        today = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        yesterday = calendar.getTime();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        year = calendar.getTime();
        return new Date[]{today, yesterday, year};
    }

    public static void scrollListView(final ListView listView, final int index, final int time) {
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.currentThread().sleep(time);
                } catch (InterruptedException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (listView != null && index >= 0) {
                    listView.setSelection(index);
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (listView != null && index >= 0) {
                    listView.setSelection(index);
                }
            }
        }.execute();
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    public static void showToast(int resId) {
        Toast.makeText(MainApplication.getInstance(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(String msg) {
        Toast.makeText(MainApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }


    public static String readStream(BufferedInputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            return new String(byteArrayOutputStream.toByteArray(), CHARSET.name());
        } catch (Exception e) {
            return "";
        } catch (Error e) {
            return "";
        } finally {
            inputStream.close();
        }
    }


    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }


    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }


    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

   /* public static String convertSinaBlogTitle(News news){
        if (news.from_str.equals("新浪博客")){
            try {
                news.title = new String(news.title.getBytes("iso-8859-1"),"gbk");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return news.title;
    }

    public static boolean encodingIsUTF8(String str) {
        String encode = "UTF-8";
        try {
           return str.equals(new String(str.getBytes(encode), encode));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }*/


   public static Bitmap drawableToBitmap(Drawable drawable) {
       Bitmap bitmap = Bitmap.createBitmap(
               drawable.getIntrinsicWidth(),
               drawable.getIntrinsicHeight(),
               drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                       : Bitmap.Config.RGB_565);
       Canvas canvas = new Canvas(bitmap);
       //canvas.setBitmap(bitmap);
       drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
       drawable.draw(canvas);
       return bitmap;
   }


    public static Bitmap getBitMBitmap(String urlpath) {
        Bitmap map = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            map = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
//===================================================================================
    /**
     * widget method
     */
    /**
     * dynamic set margin
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


    //================================Share bmp zip
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {

        int i;
        int j;
        if (bmp==null){

        }
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0,i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                //F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }

    /**
     * 高斯模糊
     *
     * @param bmp
     * @return
     */
    public static Bitmap convertToBlur(Bitmap bmp) {
        // 高斯矩阵
        int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap newBmp = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int pixColor = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int delta = 18; // 值越小图片会越亮，越大则越暗
        int idx = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                idx = 0;
                for (int m = -1; m <= 1; m++) {
                    for (int n = -1; n <= 1; n++) {
                        pixColor = pixels[(i + m) * width + k + n];
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);
                        newR = newR + pixR * gauss[idx];
                        newG = newG + pixG * gauss[idx];
                        newB = newB + pixB * gauss[idx];
                        idx++;
                    }
                }
                newR /= delta;
                newG /= delta;
                newB /= delta;
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
                newR = 0;
                newG = 0;
                newB = 0;
            }
        }
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }
}
