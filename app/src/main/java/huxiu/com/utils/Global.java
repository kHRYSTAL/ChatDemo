package huxiu.com.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import huxiu.com.model.User;


/**
 * Created by khrystal on 14/11/19.
 */
public class Global {
    public static final int avatarColor = Color.rgb(221,221,221);
    public static final int HAS_READ = 1;
    public static final int NOT_READ = 0;

    public static String BAIDU_USER_ID;
    public static String BAIDU_CHANNEL_ID;

    public static String DeviceUniqueID;

    public static User currentUser;

    public static File cacheJsonDir;

    public static int currentChatUid;
    public static int currentProfileUid;

    public static String appVersionName;
    public static String appVersionCode;
    public static String osVersion;
    public static String imei;
    public static long currentMaxMid;
    public static boolean fromLocal=false;

    private static final ThreadLocal<Gson> reuseGson = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new Gson();
        }
    };

    public static long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public static void appInit(Context context) {
        MobclickAgent.updateOnlineConfig(context);
        if (Global.DeviceUniqueID == null) {
            Global.DeviceUniqueID = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            if (Global.DeviceUniqueID == null) {
                Global.DeviceUniqueID = String.valueOf(System.currentTimeMillis() % 6000);
            }
        }

        File path = context.getExternalCacheDir();
        cacheJsonDir = new File(path, "json");
        cacheJsonDir.mkdirs();
        currentUser = Settings.getCurrentUser();

        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            appVersionName = info.versionName;
            appVersionCode = String.valueOf(info.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        osVersion = "Android_" + Build.VERSION.SDK_INT;
        imei = Utils.getIMEI(NumarkApplication.getInstance());

    }

    public static Gson getGson() {
        return reuseGson.get();
    }

    public static int getCurrentUid() {
        if (currentUser != null)
            return currentUser.uid;
        return 0;
    }

    public static boolean isLoggedIn() {
        return getCurrentUid() != 0;
    }

    public static Context getContext() {
        return NumarkApplication.getInstance();
    }
}
