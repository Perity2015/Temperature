package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.qrcode.BaseCaptureActivity;
import com.huiwu.temperaturecontrol.CaptureActivity;
import com.huiwu.temperaturecontrol.MainActivity;
import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;
import com.huiwu.temperaturecontrol.bluetooth.DeviceListActivity;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private MainActivity mainActivity;

    @Bind(R.id.image_qrcode)
    ImageView imageQrcode;

    public static final int REQUEST_QRCODE = 201;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.image_qrcode)
    public void onClick() {
        startActivityForResult(new Intent(getContext(), CaptureActivity.class), REQUEST_QRCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_QRCODE == requestCode && resultCode == Activity.RESULT_OK) {
            checkQrcode(data.getStringExtra(BaseCaptureActivity.RESULT), data.getBooleanExtra(CaptureActivity.NFC_MODE, false));
        }
    }

    private void checkQrcode(final String qrcode, boolean nfc_mode) {
        mainActivity.cancelConnectionTask();
        HashMap<String, String> map = mainActivity.getDefaultMap();
        if (nfc_mode) {
            map.put("tmprfid", qrcode);
        } else {
            map.put("boxno", qrcode);
        }
        mainActivity.task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                mainActivity.progressDialog.setMessage(getString(R.string.check_box_status_load));
                mainActivity.progressDialog.show();
            }

            @Override
            public void sendFinish() {
                mainActivity.progressDialog.dismiss();
            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                TLog.d("DEBUG", result);
                JSONModel.ReturnObject returnObject = mainActivity.gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(), getContext());
                    return;
                }
                JsonObject jsonObject = returnObject.getM_ReturnOBJJsonObject();
                String option = jsonObject.get("option").getAsString();
                if (TextUtils.equals("unBind", option)) {
                    showUnbindDialog();
                    return;
                }
                if (TextUtils.equals(ManageActivity.OPTION_NEW_BOX, option)) {
                    if (!mainActivity.userInfo.isHaveAddBox()) {
                        Utils.showLongToast("无权限添加心想提", getContext());
                        return;
                    }
                }
                JSONModel.Box box;
                if (jsonObject.has("box")) {
                    box = mainActivity.gson.fromJson(jsonObject.get("box"), JSONModel.Box.class);
                } else {
                    box = new JSONModel.Box();
                    box.setBoxno(qrcode);
                }
                Intent intent = new Intent(getContext(), ManageActivity.class);
                intent.putExtra(ManageActivity.BOX_EXTRA, box);
                intent.putExtra(ManageActivity.OPTION_EXTRA, option);
                if (jsonObject.has("boxlink")) {
                    intent.putExtra(ManageActivity.TMP_LINK_EXTRA, mainActivity.gson.fromJson(jsonObject.get("boxlink"), JSONModel.TempLink.class));
                }
                startActivity(intent);
            }

            @Override
            public void sendLost(String result) {
                mainActivity.loginAgain();
            }
        });
        mainActivity.task.execute(Constants.check_box_status);
    }

    private void showUnbindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(getResources().getStringArray(R.array.unbindItems), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent_nfc = new Intent(getContext(), NfcActivity.class);
                        intent_nfc.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_UNBIND);
                        startActivity(intent_nfc);
                        break;
                    case 1:
                        Intent intent_ble = new Intent(getContext(), DeviceListActivity.class);
                        intent_ble.putExtra(DeviceListActivity.BLE_MANAGE, DeviceListActivity.BLE_UNBIND);
                        startActivity(intent_ble);
                        break;
                }
            }
        });
        builder.show();
    }
}
