package com.huiwu.temperaturecontrol.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.huiwu.temperaturecontrol.BaseActivity;
import com.huiwu.temperaturecontrol.application.MainApp;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.sqlite.SQLiteManage;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {
    public BaseActivity baseActivity;

    public Context mContext;
    public SharedPreferences mShared;
    public Gson gson = new Gson();
    public JSONModel.UserInfo userInfo;
    public SQLiteManage sqLiteManage;

    public ProgressDialog progressDialog;
    public MainApp mainApp;
    public NfcAdapter mNfcAdapter;

    public BaseFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (BaseActivity) getActivity();
        mContext = getContext();
        mShared = baseActivity.mShared;
        userInfo = baseActivity.userInfo;
        sqLiteManage = baseActivity.sqLiteManage;
        progressDialog = baseActivity.progressDialog;
        mainApp = baseActivity.mainApp;
        mNfcAdapter = baseActivity.mNfcAdapter;
    }
}
