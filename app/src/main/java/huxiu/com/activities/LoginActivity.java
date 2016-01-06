package huxiu.com.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import huxiu.com.noomarkchatdemo.R;

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
                //TODO 登录app服务器
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

    private void doLogInAppServer(final Map<String, Object> params) {
        new RequestServerTask<LogInResponse>(LogInResponse.class, this, getString(R.string.log_in_please_wait)) {
            @Override
            protected String requestServer() {
                return HttpUtil.post(NetworkConstants.LOG_IN_URL, params);
            }

            @Override
            protected void onSuccess(LogInResponse result) {
                saveUserInfo(result);
                if (result.data.status == Constants.REGISTER_COMPLETE) {
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    startActivity(intent);
                }else if(result.data.status == Constants.PASSWORD_COMPLETE){
                    Intent intent = new Intent(LogInActivity.this, ChooseInterestsActivity.class);
                    intent.putExtra("user", Global.getGson().toJson(result.data));
                    intent.putExtra(EXTRA_FROM_LOGIN,true);
                    startActivity(intent);
                }else  if (result.data.status == Constants.INTEREST_COMPLETE){
                    Intent intent = new Intent(LogInActivity.this,AddFriendsActivity.class);
                    intent.putExtra(EXTRA_FROM_LOGIN,true);
                    startActivity(intent);
                }

                finish();
            }
        }
                .setDefaultErrMsg(R.string.log_in_failed)
                .start();
    }
}
