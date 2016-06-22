package com.huiwu.temperaturecontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
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
    @Bind(R.id.image_login)
    ImageView imageLogin;

    private boolean loginAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

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

        imageLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        loginAgain = getIntent().getBooleanExtra(Constants.LOGIN_AGAIN, false);

        if (userInfo != null && userInfo.getM_UserInfo() != null) {
            String password = userInfo.getM_UserInfo().getPassword();
            String username = userInfo.getM_UserInfo().getUsername();
            editUsername.setText(username);
            if (!TextUtils.isEmpty(password)) {
                editPassword.setText(password);
                if (!loginAgain) {
                    startActivity(new Intent(mContext, MainActivity.class));
                    finish();
                    return;
                }
            }
        }

        editUsername.addTextChangedListener(new Watcher(R.id.edit_username));
        editPassword.addTextChangedListener(new Watcher(R.id.edit_password));

        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSoftInput();
                    String username = editUsername.getText().toString().trim();
                    String password = editPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                        return true;
                    }
                    login(username, password);
                    return true;
                }
                return false;
            }
        });
    }

    private int clickTimes = 0;
    private long clickTime = 0;

    private void showEditDialog() {
        if (System.currentTimeMillis() - clickTime > 1000) {
            clickTimes = 1;
        } else {
            clickTimes += 1;
        }
        clickTime = System.currentTimeMillis();
        if (clickTimes < 10) {
            return;
        }
        clickTimes = 0;
        final EditText editText = new EditText(mContext);
        editText.setSingleLine();
        editText.setText(Constants.HOST);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(editText);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String site = editText.getText().toString().trim();
                Constants.HOST = site;
            }
        });
        builder.show();
    }

    private void login(final String username, final String password) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        ConnectionUtil.postParams(Constants.HOST + Constants.LOGIN_URL, map, new StringConnectionCallBack() {
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
                userInfo = gson.fromJson(returnObject.getM_ReturnOBJJsonObject(), JSONModel.UserInfo.class);
                userInfo.getM_UserInfo().setPassword(password);
                String enToStr = Base64.encodeToString(gson.toJson(userInfo).getBytes(), Base64.DEFAULT);
                mShared.edit().putString(Constants.USER_INFO, enToStr).commit();

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
