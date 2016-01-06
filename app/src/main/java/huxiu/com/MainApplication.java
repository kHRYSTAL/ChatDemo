package huxiu.com;

import android.app.Application;

/**
 * Created by Yao on 2016/1/6.
 */
public class MainApplication extends Application{

    private static MainApplication sInstance;
    public static MainApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

    }
}
