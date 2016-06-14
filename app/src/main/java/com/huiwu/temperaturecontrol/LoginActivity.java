package com.huiwu.temperaturecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    @Bind(R.id.edit_username)
    EditText editUsername;
    @Bind(R.id.edit_password)
    EditText editPassword;
    @Bind(R.id.btn_login)
    Button btnLogin;

    private boolean loginAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginAgain = getIntent().getBooleanExtra(Constants.login_again, false);

        editUsername.addTextChangedListener(new Watcher(R.id.edit_username));
        editPassword.addTextChangedListener(new Watcher(R.id.edit_password));

        if (userInfo != null) {
            String password = userInfo.getPassword();
            String username = userInfo.getUsername();
            editUsername.setText(username);
            if (!TextUtils.isEmpty(password)) {
                editPassword.setText(password);
            }
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    return;
                }
                login(username, password);
            }
        });

        if (!loginAgain && userInfo != null && !TextUtils.isEmpty(userInfo.getPassword())) {
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        } else {
            if (loginAgain)
                Utils.showLongToast(R.string.please_login_again, mContext);
        }
    }

    private void login(final String username, final String password) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        ConnectionUtil.postParams(Constants.login_url, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage(getString(R.string.login_load));
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = gson.fromJson(s, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(), mContext);
                    return;
                }
                JsonObject UserInfoJsonObject = returnObject.getM_ReturnOBJJsonObject().getAsJsonObject("m_UserInfo");
                JsonObject UserPowerJsonObject = returnObject.getM_ReturnOBJJsonObject().getAsJsonObject("message");
                userInfo = gson.fromJson(UserInfoJsonObject, JSONModel.UserInfo.class);
                userInfo.setPassword(password);
                userInfo.setHaveAddBox(returnObject.getM_ReturnOBJJsonObject().get("HaveAddBox").getAsBoolean());
                JSONModel.UserInfo.UserPower userPower = gson.fromJson(UserPowerJsonObject, JSONModel.UserInfo.UserPower.class);
                userInfo.setUserPower(userPower);

                mShared.edit().putString(Constants.user_info, gson.toJson(userInfo)).commit();

                if (loginAgain) {
                    finish();
                    return;
                }
                startActivity(new Intent(mContext, MainActivity.class));
                finish();
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, mContext);
            }

            @Override
            public void onLost() {

            }

        });
    }

    private class Watcher implements TextWatcher {
        private int id;

        public Watcher(int id) {
            super();
            this.id = id;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                return;
            }
            switch (id) {
                case R.id.edit_username:
                    editPassword.setText("");
                    break;
                case R.id.edit_password:
                    if (TextUtils.isEmpty(editUsername.getText().toString().trim())) {
                        editPassword.setText("");
                    }
                    break;
            }
        }
    }
}
