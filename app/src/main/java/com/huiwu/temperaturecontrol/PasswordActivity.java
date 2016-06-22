package com.huiwu.temperaturecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

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

public class PasswordActivity extends BaseActivity {

    @Bind(R.id.edit_old_password)
    EditText editOldPassword;
    @Bind(R.id.edit_new_password)
    EditText editNewPassword;
    @Bind(R.id.edit_confirm_password)
    EditText editConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                hideSoftInput();
                checkComplete();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void checkComplete() {
        String oldPassword = editOldPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(oldPassword) || (TextUtils.isEmpty(newPassword)) || TextUtils.isEmpty(confirmPassword)) {
            Utils.showLongToast(getString(R.string.incomplete_information), mContext);
            return;
        } else if (!TextUtils.equals(newPassword, confirmPassword)) {
            Utils.showLongToast("两次密码不一致", mContext);
            return;
        } else if (TextUtils.equals(oldPassword, newPassword)) {
            Utils.showLongToast("新旧密码一致，无需修改", mContext);
            return;
        }
        changePassword(oldPassword, newPassword);
    }

    private void changePassword(String oldPassword, String newPassword) {
        final HashMap<String, String> map = getDefaultMap();
        map.put("pwd", oldPassword);
        map.put("newpwd", newPassword);
        ConnectionUtil.postParams(Constants.HOST + Constants.REVISE_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage("提交信息中");
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = gson.fromJson(s, JSONModel.ReturnObject.class);
                Utils.showLongToast(returnObject.getsMsg(), mContext);

                if (!returnObject.isbOK()) {
                    return;
                }
                userInfo.getM_UserInfo().setPassword("");
                String enToStr = Base64.encodeToString(gson.toJson(userInfo).getBytes(), Base64.DEFAULT);
                mShared.edit().putString(Constants.USER_INFO, enToStr).commit();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, mContext);
            }

            @Override
            public void onLost() {
                loginAgain();
            }

        });
    }
}
