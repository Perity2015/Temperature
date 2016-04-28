package com.huiwu.temperaturecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

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

        if (!loginAgain && userInfo != null) {
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        } else {
            if (loginAgain)
                Utils.showLongToast(R.string.please_login_again,mContext);
        }
    }

    private void login(final String username, final String password) {
        cancelConnectionTask();
        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage(getString(R.string.login_load));
                progressDialog.show();
            }

            @Override
            public void sendFinish() {
                progressDialog.dismiss();
            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                TLog.d("LL", result);
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(),mContext);
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
            public void sendLost(String result) {

            }


        });
        task.execute(Constants.login_url);
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
