package huxiu.com.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.Calendar;

import huxiu.com.model.User;


/**
 * Created by yao on 15-7-24:上午7:43.
 */
public class Settings {

    public static final String PREF_NAME = "preferences";
    public static final String VERSION_KEY = "version_key";
    private static final String TOKEN_KEY = "token";

    public static final String USER_KEY = "uid_";
    public static final String CURR_USER_KEY = "currUser";
    public static final String UIDS_KEY = "uids";



    public static SharedPreferences getPrefs() {
        int code = Context.MODE_MULTI_PROCESS;
        return Global.getContext().getSharedPreferences(PREF_NAME, code);
    }

    public static SharedPreferences.Editor getPrefsEditor() {
        return getPrefs().edit();
    }

    public static String[] getUids() {
        SharedPreferences prefs = getPrefs();
        String data = prefs.getString(UIDS_KEY, "");
        if (TextUtils.isEmpty(data))
            return new String[0];
        return data.split(",");
    }

    public static void saveUser(User user) {
        Gson gson = Global.getGson();
        String profile = gson.toJson(user);
        SharedPreferences prefs = getPrefs();
        String data = prefs.getString(UIDS_KEY, "");

        if (TextUtils.isEmpty(data)) {
            data = String.valueOf(user.uid);
        } else {
            String[] uids = data.split(",");
            for (String uid : uids) {
                if (Integer.parseInt(uid) == user.uid) {
                    return;
                }
            }
            data = data + "," + user.uid;
        }
        SharedPreferences.Editor edit = getPrefsEditor();
        edit.putString(USER_KEY + user.uid, profile);
        edit.putString(UIDS_KEY, data);
        edit.commit();
    }

    public static void delUser(String uidToDel) {
        SharedPreferences prefs = getPrefs();
        String[] uids = getUids();
        if (uids.length == 0)
            return;
        StringBuilder sb = new StringBuilder();
        for (String uid : uids) {
            if (uid.equals(uidToDel)) {
                continue;
            }
            if (sb.length() > 0)
                sb.append(',');
            sb.append(uid);
        }
        SharedPreferences.Editor edit = getPrefsEditor();
        edit.remove(USER_KEY + uidToDel);
        edit.putString(UIDS_KEY, sb.toString());
        edit.commit();
    }

    public static void setCurrentUid(int uid) {
        SharedPreferences.Editor edit = getPrefsEditor();
        edit.putInt(CURR_USER_KEY, uid);
        edit.commit();
    }

    public static User getUser(String uid) {
        SharedPreferences prefs = getPrefs();
        String profile = prefs.getString(USER_KEY + uid, "");
        Gson gson = Global.getGson();
        User user = gson.fromJson(profile, User.class);
        return user;
    }

    public static User getCurrentUser() {
        SharedPreferences prefs = getPrefs();
        int uid = prefs.getInt(CURR_USER_KEY, 0);
        if (uid == 0) {
            return null;
        }
        return getUser(String.valueOf(uid));
    }
}
