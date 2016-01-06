package huxiu.com.utils;

import android.content.Context;

import applib.model.DefaultHXSDKModel;

/**
 * Created by ASUS on 2016/1/6.
 */
public class NMHXSDKModel extends DefaultHXSDKModel{

    public NMHXSDKModel(Context ctx) {
        super(ctx);
    }

    // demo will switch on debug mode
    public boolean isDebugMode(){
        return true;
    }
}
