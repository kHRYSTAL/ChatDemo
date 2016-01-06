package huxiu.com.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kHRYSAL on 15-7-24.
 */
public class HttpUtil {
    public static final int CONNECTION_TIME_OUT = 5000;
    public static final int READ_TIME_OUT = 5000;
    public static final String USER_AGENT = "Numark_Android";
    public static final int IO_BUFFER_SIZE = 1024 * 16;

    public static final String BOUNDARY = "----------Sneu1XNumark_kHRYSTAL_n1ufMEI";
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int BUFFER_SIZE = 4096;
    private static final String LOG_TAG = HttpUtil.class.getName();

    public static String get(String uri, Map<String, Object> parameters) {
        return post(uri, parameters, null, null);
    }

    public static String get(String uri, Map<String, Object> parameters, Map<String, String> headers) {
        return post(uri, parameters, null, headers);
    }

    public static String post(String uri, Map<String, Object> parameters) {
        return post(uri, null, parameters, null);
    }

    public static String post(String uri, Map<String, Object> parameters, Map<String, Object> postParameters, Map<String, String> headers) {
        String responseBody = "";
        if (uri == null || uri.trim().length() == 0) {
            Log.e(LOG_TAG, "invalid url " + String.valueOf(uri));
            return null;
        }
        StringBuilder urlStringBuilder = new StringBuilder(uri);
        if (parameters != null) {
            int i = 0;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key != null && value != null) {
                    if (value instanceof Object[]) {
                        Object[] values = (Object[]) value;
                        for (Object v : values) {
                            appendParameter(urlStringBuilder, i, key, v);
                            i++;
                        }
                    } else {
                        appendParameter(urlStringBuilder, i, key, value);
                        i++;
                    }
                }
            }
        }
        HttpURLConnection connection = null;
        long s = System.currentTimeMillis();
        String urlString = urlStringBuilder.toString();
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(READ_TIME_OUT);
            connection.setConnectTimeout(CONNECTION_TIME_OUT);
            connection.setRequestProperty("Charset", CHARSET.name());
            Log.d("charset==",CHARSET.name());
            if (Global.currentUser != null) {
                connection.addRequestProperty("uid", String.valueOf(Global.currentUser.uid));
                connection.addRequestProperty("token", String.valueOf(Global.currentUser.token));
            }
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(), header.getValue());
                }
            }
            connection.addRequestProperty("appVersionName", Global.appVersionName);
            connection.addRequestProperty("appVersionCode", Global.appVersionCode);
            connection.addRequestProperty("os", Global.osVersion);
            connection.addRequestProperty("imei", Global.imei);
            if (postParameters != null && postParameters.size() > 0) {
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                writeStream(new BufferedOutputStream(os), postParameters);
            }

            responseBody = readStream(new BufferedInputStream(connection.getInputStream()));
            LogUtils.d(LOG_TAG, String.format("get %s success", uri));
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, new StringBuilder().append("MalformedURLException ").append(e != null ? String.valueOf(e.getMessage()) : "").toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, new StringBuilder().append("IOException ").append(e != null ? String.valueOf(e.getMessage()) : "").toString());
        } catch (Exception e) {
            Log.e(LOG_TAG, new StringBuilder().append("Exception ").append(e != null ? String.valueOf(e.getMessage()) : "").toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {

                LogUtils.i(LOG_TAG, new StringBuilder()
                        .append("request get\nurl:")
                        .append(urlString)
                        .append("\ntotal spends:")
                        .append(String.valueOf(System.currentTimeMillis() - s))
                        .append(" ms")
                        .append("\nresponse size:")
                        .append(String.valueOf(responseBody.getBytes().length))
                        .append("\nresponse:")
                        .append(responseBody).toString());

            } catch (Exception e) {
            } catch (Error e) {
            }
        }
        return responseBody;
    }

    public static byte[] getInputStream(String uri, Map<String, Object> parameters) {
        LogUtils.d(LOG_TAG, String.format("start get %s", uri));
        if (uri == null || uri.trim().length() == 0) {
            Log.e(LOG_TAG, "invalid url " + String.valueOf(uri));
            return null;
        }
        StringBuilder urlStringBuilder = new StringBuilder(uri);
        int i = 0;
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key != null && value != null) {
                    if (value instanceof Object[]) {
                        Object[] values = (Object[]) value;
                        for (Object v : values) {
                            appendParameter(urlStringBuilder, i, key, v);
                            i++;
                        }
                    } else {
                        appendParameter(urlStringBuilder, i, key, value);
                        i++;
                    }
                }
            }
        }
        HttpURLConnection connection = null;
        long s = System.currentTimeMillis();
        String urlString = urlStringBuilder.toString();
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(READ_TIME_OUT);
            connection.setConnectTimeout(CONNECTION_TIME_OUT);
            connection.setRequestProperty("Charset", CHARSET.name());
            byte[] ret = read(connection.getInputStream());
            LogUtils.d(LOG_TAG, String.format("get %s success", uri));
            return ret;
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, new StringBuilder().append("MalformedURLException ").append(e != null ? String.valueOf(e.getMessage()) : "").toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, new StringBuilder().append("IOException ").append(e != null ? String.valueOf(e.getMessage()) : "").toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            LogUtils.i(LOG_TAG, new StringBuilder()
                    .append("request get\nurl:")
                    .append(urlString)
                    .append("\ntotal spends:")
                    .append(String.valueOf(System.currentTimeMillis() - s))
                    .append(" ms")
                    .toString());
        }
        return null;
    }

    private static void appendParameter(StringBuilder urlStringBuilder, int i, String key, Object value) {
        if (i == 0 && urlStringBuilder.indexOf("?") < 0) {
            urlStringBuilder.append("?");
        } else if (i > 0 || urlStringBuilder.indexOf("?") < urlStringBuilder.length() - 1) {
            urlStringBuilder.append("&");
        }
        urlStringBuilder
                .append(Uri.encode(key))
                .append("=")
                .append(Uri.encode(String.valueOf(value)));
    }

    private static void writeStream(BufferedOutputStream outputStream, Map<String, Object> parameters) throws IOException {
        try {
            int i = 0;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (i > 0) {
                    outputStream.write("&".getBytes(CHARSET.name()));
                }
                outputStream.write(entry.getKey().getBytes(CHARSET.name()));
                outputStream.write("=".getBytes(CHARSET.name()));
                if (entry.getValue() instanceof byte[]) {
                    outputStream.write((byte[]) entry.getValue());
                } else if (entry.getValue() instanceof File) {
                    File uploadFile = (File) entry.getValue();
                    if (uploadFile.exists()) {
                        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(uploadFile));
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count = 0;
                        while ((count = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, count);
                        }
                    }
                } else {
                    outputStream.write(Uri.encode(String.valueOf(entry.getValue()), CHARSET.name()).getBytes());
                }
                i++;
            }
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }

    public static byte[] read(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (inputStream != null) {
            int nRead;
            byte[] data = new byte[IO_BUFFER_SIZE];
            try {
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            } catch (Exception e) {
                Log.e(LOG_TAG, String.valueOf(e));
            } catch (Error e) {
                Log.e(LOG_TAG, String.valueOf(e));
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private static String readStream(BufferedInputStream inputStream) throws IOException {
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

    public static String postReturnString(Context context, String url, Map<String, Object> parameters, boolean isGzip) {
        String responseBody = "";
        if (url != null && parameters != null && parameters.size() > 0) {
            long s = System.currentTimeMillis();
            Log.i(LOG_TAG, new StringBuilder(128).append("begin post ").append(url).toString());
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT, context);
            try {
                HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpClient.getParams(), READ_TIME_OUT);
                List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>(parameters.size());
                for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet()) {
                    String key = stringObjectEntry.getKey();
                    Object value = stringObjectEntry.getValue();
                    if (key != null && value != null) {
                        list.add(new BasicNameValuePair(key, String.valueOf(value)));
                    }
                }

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, CHARSET.name());
                HttpPost httpPost = new HttpPost(url);
                if (isGzip) {
//                    httpPost.addHeader("Content-Encoding", "gzip");
                    httpPost.setEntity(AndroidHttpClient.getCompressedEntity(EntityUtils.toByteArray(entity), null));
                } else {
                    httpPost.setEntity(entity);
                }

                HttpResponse httpResponse = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(httpResponse.getEntity());
            } catch (IOException e) {
                Log.e(LOG_TAG, String.valueOf(e));
//                responseBody = Global.ErrorCode.toString(e);
            } finally {
                StringBuilder logInfo = new StringBuilder()
                        .append("request post\nurl:")
                        .append(url);
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    logInfo.append("\n")
                            .append(entry.getKey())
                            .append("\t")
                            .append(String.valueOf(entry.getValue()));
                }
                logInfo.append("\ntotal spends:")
                        .append(String.valueOf(System.currentTimeMillis() - s))
                        .append(" ms")
                        .append("\nresponse size:")
                        .append(String.valueOf(responseBody.getBytes().length))
                        .append("\nresponse:")
                        .append(responseBody);
                LogUtils.i(LOG_TAG, logInfo.toString());
                httpClient.close();
            }
        } else {
//            responseBody = Global.ErrorCode.toString(Global.ErrorCode.PARAMETER_INVALID);
        }
        return responseBody;
    }


    public static String postMultiPartReturnString(Context context, String url, Map<String, Object> parameters) {
        String responseBody = "";
        if (url != null && parameters != null) {
            long s = System.currentTimeMillis();
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT, context);
            try {
                HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpClient.getParams(), READ_TIME_OUT);
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, CHARSET);
                for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet()) {
                    String name = stringObjectEntry.getKey();
                    Object value = stringObjectEntry.getValue();
                    if (name != null && value != null) {
                        if (value instanceof File) {
                            multipartEntity.addPart(name, new FileBody((File) value, "application/octet-stream"));
                        } else if (value instanceof Bitmap) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            ((Bitmap) value).compress(Bitmap.CompressFormat.PNG, 100, stream);
                            multipartEntity.addPart(name, new ByteArrayBody(stream.toByteArray(), String.valueOf(System.nanoTime())));
                        } else if (value instanceof byte[]) {
                            multipartEntity.addPart(name, new ByteArrayBody(((byte[]) value), String.valueOf(System.nanoTime())));
                        } else if (value instanceof InputStream) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            InputStream inputStream = (InputStream) value;
                            byte[] bytes = new byte[2048];
                            while (inputStream.read(bytes) > 0) {
                                stream.write(bytes);
                            }
                            multipartEntity.addPart(name, new ByteArrayBody(stream.toByteArray(), String.valueOf(System.nanoTime())));
                        } else {
                            multipartEntity.addPart(new FormBodyPart(name, new StringBody(String.valueOf(value), CHARSET)));
                        }
                    }
                }
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(multipartEntity);
                httpPost.addHeader("Content-Type", "multipart/form-data;boundary=".concat(BOUNDARY));
                httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(httpResponse.getEntity());
                Log.i(LOG_TAG, "post response:" + responseBody);
            } catch (IOException e) {
                Log.e(LOG_TAG, String.valueOf(e));
//                responseBody = Global.ErrorCode.toString(e);
            } finally {
                StringBuilder logInfo = new StringBuilder()
                        .append("request post\nurl:")
                        .append(url);
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    logInfo.append("\n")
                            .append(entry.getKey())
                            .append("\t")
                            .append(String.valueOf(entry.getValue()));
                }
                logInfo.append("\ntotal spends:")
                        .append(String.valueOf(System.currentTimeMillis() - s))
                        .append(" ms")
                        .append("\nresponse size:")
                        .append(String.valueOf(responseBody.getBytes().length))
                        .append("\nresponse:")
                        .append(responseBody);
                LogUtils.i(LOG_TAG, logInfo.toString());
                httpClient.close();
            }
        } else {
//            responseBody = Global.ErrorCode.toString(Global.ErrorCode.PARAMETER_INVALID);
        }
        return responseBody;
    }

//    public static Bitmap getBitmapFromURL(String imageURL) {
//        HttpURLConnection connection = null;
//        try {
//            connection = (HttpURLConnection) (new URL(imageURL)).openConnection();
//            connection.setReadTimeout(READ_TIME_OUT);
//            connection.setConnectTimeout(CONNECTION_TIME_OUT);
//            return BitmapUtil.decodeStreamLargeBitmap(connection.getInputStream());
//        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG, new StringBuilder().append("MalformedURLException ").append(e != null ? String.valueOf(e.getMessage()) : "").append("\n").append(imageURL).toString());
//        } catch (IOException e) {
//            Log.e(LOG_TAG, new StringBuilder().append("IOException ").append(e != null ? String.valueOf(e.getMessage()) : "").append("\n").append(imageURL).toString());
//        } catch (Error e) {
//            Log.e(LOG_TAG, new StringBuilder().append(String.valueOf(e)).append("\n").append(imageURL).toString());
//        } finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }
//        return null;
//    }


    public static boolean downloadFile(String url, String targetPath) {
        HttpURLConnection connection = null;
        OutputStream output = null;

        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setReadTimeout(READ_TIME_OUT);
            connection.setConnectTimeout(CONNECTION_TIME_OUT);
            InputStream is = connection.getInputStream();
            File file = new File(targetPath);

            final byte[] buffer = new byte[1024];
            int read;

            output = new FileOutputStream(file);
            try {
                while ((read = is.read(buffer)) != -1)
                    output.write(buffer, 0, read);

                output.flush();
            } finally {
                if (output != null)
                    output.close();
            }

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, new StringBuilder("downloadFile: \n").append(String.valueOf(e)).append("\n").append(url).toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return false;
    }
}
