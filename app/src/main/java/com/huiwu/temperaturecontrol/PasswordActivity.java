package com.huiwu.temperaturecontrol;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_back);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
        cancelConnectionTask();
        final HashMap<String, String> map = getDefaultMap();
        map.put("pwd", oldPassword);
        map.put("newpwd", newPassword);
        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage("提交信息中");
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
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(), mContext);
                    return;
                }
                userInfo.setPassword("");
                mShared.edit().putString(Constants.user_info, gson.toJson(userInfo)).commit();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void sendLost(String result) {

            }
        });
        task.execute(Constants.revise_url);
    }
}
