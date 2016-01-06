package huxiu.com.model;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;

/**
 * Created by kHRYSTAL on 14/10/24:下午7:38.
 */
public class User {

    public int uid;

    public String pic;

    public boolean is_QQ;

    public String weixin;

    public boolean is_email;

    public boolean is_at;

    public String message_interval;

    public boolean is_phone;

    public boolean is_comment;

    public boolean is_like;

    public boolean is_topic;

    public String phone;

    public String username;

    public String nick;

    public int fans;

    public int follow;

    public int from_type;

    public int friend_type;

    public String des;

    public int status;

    public String cellphone;

    public String QQ;

    public String weibo;

    public boolean is_follow;

    public String body;

    public int sex;

    public String email;

    //public String wechat;

    public boolean is_mine;

    public String token;

    public String p_nick;

    public boolean is_forward;

    public boolean is_fans;

    public boolean topic_secret;

    public boolean like_secret;

    public boolean is_weibo;

    public boolean is_weixin;

    public boolean is_label;

    public boolean is_dialog;

    //2.2新增字段 用于标识好友也关注了这个人
    public String friend_nick;


    public String birthday;

    public static final int FROM_TYPE_SYSTEM = 0;

    public static final int FROM_TYPE_REGISTERED = 1;

    public static final int FROM_TYPE_WEIBO = 2;

    public static final int FROM_TYPE_QQ = 3;

    public static final int FRIEND_TYPE_SYSTEM = 0;

    public static final int FRIEND_TYPE_CONTACTS = 1;

    public static final int FRIEND_TYPE_WEIBO = 2;

    public static final int FRIEND_TYPE_QQ = 3;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        return uid == user.uid;
    }
}
