package huxiu.com;

import android.app.Application;

import com.easemob.EMCallBack;

import huxiu.com.utils.NMHXSDKHelper;

/**
 * Created by Yao on 2016/1/6.
 */
public class MainApplication extends Application{

    private static MainApplication sInstance;
    public static MainApplication getInstance() {
        return sInstance;
    }

    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";
    public static NMHXSDKHelper hxSDKHelper = new NMHXSDKHelper();

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        /**
         * this function will initialize the HuanXin SDK
         *
         * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
         *
         * 环信初始化SDK帮助函数
         * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
         *
         * for example:
         * 例子：
         *
         * public class DemoHXSDKHelper extends HXSDKHelper
         *
         * HXHelper = new DemoHXSDKHelper();
         * if(HXHelper.onInit(context)){
         *     // do HuanXin related work
         * }
         */
        hxSDKHelper.onInit(this);
    }

    /**
     * 退出登录,清空数据
     */
    public void logout(final boolean isGCM,final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(isGCM,emCallBack);
    }
}
