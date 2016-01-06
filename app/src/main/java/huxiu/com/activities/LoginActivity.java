package huxiu.com.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import huxiu.com.R;
import huxiu.com.model.LogInResponse;
import huxiu.com.net.NetworkConstants;
import huxiu.com.net.RequestServerTask;
import huxiu.com.utils.Global;
import huxiu.com.utils.HttpUtil;
import huxiu.com.utils.Settings;

/**
 * 登录界面
 */

/**
 * 输入用户名密码
 * 需要做判断是否为空或不符合规则(这里不做判断)
 * 然后进行登陆app服务器操作，登陆app服务器成功后需要登录环信服务器
 * 表示用户上线
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etUsername;
    EditText etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText)findViewById(R.id.login_username);
        etPassword = (EditText)findViewById(R.id.login_username);
        btnLogin = (Button)findViewById(R.id.login_login);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_login:

                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("username", username);
                    params.put("password", password);
                    params.put("type", 1);
                    doLogInAppServer(params);
                }
                break;
        }
    }


    /**
     * 登录App服务器
     * @param params post请求参数
     */
    private void doLogInAppServer(final Map<String, Object> params) {
        new RequestServerTask<LogInResponse>(LogInResponse.class, this, getString(R.string.log_in_please_wait)) {
            @Override
            protected String requestServer() {
                return HttpUtil.post(NetworkConstants.LOG_IN_URL, params);
            }

            @Override
            protected void onSuccess(LogInResponse result) {
                saveUserInfo(result);
                //TODO 登录环信服务器
                //这个Demo中 环信需要的username与password为uid和明文写死的密码12345
                //实际项目中根据需要修改密码 因为主键尽量统一 在已有用户体系下使用相同主键
                doLoginHuanXinServer(result.data.uid,"12345");
            }
        }
                .setDefaultErrMsg(R.string.log_in_failed)
                .start();
    }

    /**
     * 登录环信服务器
     * 环信服务器登录所需参数
     * @param uid
     * @param password
     */
    private void doLoginHuanXinServer(final long uid,final String password) {

    }

    /**
     * 保存用户信息至内存与SharePerference
     * @param result 登录请求返回json对象
     */
    protected void saveUserInfo(LogInResponse result) {
        Settings.saveUser(result.data);
        Global.currentUser = result.data;
        Settings.setCurrentUid(result.data.uid);
    }
}
