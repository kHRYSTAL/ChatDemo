package huxiu.com.net;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

/**
 * Created by yao on 15/7/23.
 */
public abstract class RequestServerTask<T extends BaseResponse> extends AsyncTask<Void, Void, String> {
    private static final String LOG_TAG = RequestServerTask.class.getSimpleName();

    String default_err_msg;
    Class<T> responseType;
    String progressTitle;
    BaseActivity mActivity;
    Fragment mFragment;
    ProgressFragment progressFragment;
    View viewForSnackbar;
    String errorType;

    public RequestServerTask(Class<T> responseType) {
        super();
        this.responseType = responseType;
    }

    /*public RequestServerTask(Class<T> responseType, Object parent, String progressTitle,String errorType) {
        this.(Class<T> responseType,Object)
    }*/

    public RequestServerTask(Class<T> responseType, Object parent, String progressTitle) {
        super();
        this.responseType = responseType;
        if (parent instanceof BaseActivity) {
            mActivity = (BaseActivity) parent;
        } else if (parent instanceof Fragment) {
            mFragment = (Fragment) parent;
        }
        this.progressTitle = progressTitle;
    }

    public RequestServerTask setDefaultErrMsg(String errMsg) {
        this.default_err_msg = errMsg;
        return this;
    }

    public RequestServerTask setDefaultErrMsg(int errMsg) {
        this.default_err_msg = Global.getContext().getString(errMsg);
        return this;
    }

    public RequestServerTask setSnackbarView(View view) {
        this.viewForSnackbar = view;
        return this;
    }

    @Override
    protected String doInBackground(Void... params) {
        return requestServer();
    }

    protected abstract String requestServer();

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        T result;
        try {
            if (s != null) {
                result = Global.getGson().fromJson(s, responseType);
            } else {
                result = null;
            }
            if (result != null && result.ret) {
                onSuccess(result);
            } else {
                boolean handled = onFailure(result);
                if (!handled) {
                    if (viewForSnackbar != null) {
                        if (result != null && !TextUtils.isEmpty(result.msg)) {
                            Utils.showSnackbar(viewForSnackbar, result.msg);
                        } else if (default_err_msg != null) {
                            Utils.showSnackbar(viewForSnackbar, default_err_msg);
                        } else {
                            if(mFragment!=null){
                                if (mFragment instanceof NoticeCommentFragment || mFragment instanceof NoticeLikeFragment
                                        || mFragment instanceof NoticeMsgFragment){
                                    Utils.showSnackbar(viewForSnackbar, R.string.default_server_err);
                                    //return;
                                }
                            }else if (mActivity != null){
                                if (mActivity instanceof MarkActivity){
                                    Utils.showToast(R.string.default_server_err);
                                    mActivity.finish();
                                }else if (mActivity instanceof PublishActivity){
                                    Utils.showToast(R.string.default_server_err);
                                    ((PublishActivity)mActivity).rightButton.setClickable(true);
                                }
                            }else {
                                Utils.showSnackbar(viewForSnackbar, R.string.default_server_err);
                            }
                        }
                    } else {
                        if (result != null && !TextUtils.isEmpty(result.msg)) {
                            Utils.showToast(result.msg);
                        } else if (default_err_msg != null) {
                            Utils.showToast(default_err_msg);
                        } else {
                            Utils.showToast(R.string.default_server_err);
                            if (mActivity != null){
                                if (mActivity instanceof MarkActivity){
                                    mActivity.finish();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        } finally {
            onFinish();
        }
    }

    protected abstract void onSuccess(T result);

    protected void onStart() {
        if (!TextUtils.isEmpty(progressTitle)) {
            if (mActivity != null) {
                progressFragment = ProgressFragment.show(mActivity, progressTitle);
            } else if (mFragment != null && !(mFragment instanceof NoticeCommentFragment) && !(mFragment instanceof NoticeLikeFragment)) {
                progressFragment = ProgressFragment.show(mFragment, progressTitle);
            }
        }
    }

    protected void onFinish() {
        if (progressFragment != null) {
            progressFragment.dismissAllowingStateLoss();
            progressFragment.dismiss();
            progressFragment = null;
        }
    }

    protected boolean onFailure(final T result) {
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    public void start() {
        this.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }
}
