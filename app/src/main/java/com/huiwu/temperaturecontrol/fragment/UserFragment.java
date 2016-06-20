package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.huiwu.model.utils.UpdateManage;
import com.huiwu.model.utils.Utils;
import com.huiwu.model.view.CircleImageView;
import com.huiwu.model.view.MyAlertDialog;
import com.huiwu.temperaturecontrol.LoginActivity;
import com.huiwu.temperaturecontrol.PasswordActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends BaseFragment {


    @Bind(R.id.image_user)
    CircleImageView imageUser;
    @Bind(R.id.text_userInfo_name)
    TextView textUserInfoName;
    @Bind(R.id.text_userInfo_company)
    TextView textUserInfoCompany;
    @Bind(R.id.btn_change_password)
    Button btnChangePassword;
    @Bind(R.id.btn_check_update)
    Button btnCheckUpdate;
    @Bind(R.id.btn_sign_out)
    Button btnSignOut;

    private final int REQUEST_CHANGE_PASSWORD = 202;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        textUserInfoName.setText(userInfo.getM_UserInfo().getRealname());
        textUserInfoCompany.setText(userInfo.getM_UserInfo().getCompany() + "    " + userInfo.getM_UserInfo().getOrgna_name() + "\n" + userInfo.getM_UserInfo().getPowername());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CHANGE_PASSWORD) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.image_user, R.id.btn_change_password, R.id.btn_check_update, R.id.btn_sign_out})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_user:
                break;
            case R.id.btn_change_password:
                startActivityForResult(new Intent(getContext(), PasswordActivity.class), REQUEST_CHANGE_PASSWORD);
                break;
            case R.id.btn_check_update:
                UpdateManage updateManage = new UpdateManage(getContext(), progressDialog, true);
                updateManage.checkVersion(Constants.APP_NAME);
                break;
            case R.id.btn_sign_out:
                showQuitDialog();
                break;
        }
    }

    private void showQuitDialog() {
        MyAlertDialog alertDialog = new MyAlertDialog.Builder(getContext()).setTitle("退出提示")
                .setMessage("确认退出当前账号？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mShared.edit().putString(Constants.USER_INFO, "{}").commit();
                        Utils.saveUserImage(getContext(), null, Constants.USER_IMAGE);
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }
}
