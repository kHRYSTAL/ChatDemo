package huxiu.com.utils;

import android.content.Context;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;

import applib.controller.HXSDKHelper;
import applib.model.HXSDKModel;

/**
 * Created by ASUS on 2016/1/6.
 */
public class NMHXSDKHelper extends HXSDKHelper {
    @Override
    protected HXSDKModel createModel() {
        return null;
    }

    /**
     * get  HX SDK Model
     */
    public NMHXSDKModel getModel(){
        return (NMHXSDKModel) hxModel;
    }

    @Override
    public synchronized boolean onInit(Context context){
        if(super.onInit(context)){
            //if your app is supposed to user Google Push, please set project number
//            String projectNumber = "562451699741";
//            EMChatManager.getInstance().setGCMProjectNumber(projectNumber);
            return true;
        }

        return false;
    }

    @Override
    protected void initHXOptions(){
        super.initHXOptions();

        // you can also get EMChatOptions to set related SDK options
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        options.allowChatroomOwnerLeave(getModel().isChatroomOwnerLeaveAllowed());
    }
}
