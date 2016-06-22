package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.qrcode.BaseCaptureActivity;
import com.huiwu.temperaturecontrol.CaptureActivity;
import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bluetooth.DeviceListActivity;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends BaseFragment {
    @Bind(R.id.image_qrcode)
    ImageView imageQrcode;

    public static final int REQUEST_QRCODE = 201;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        HashMap<String, String> map = baseActivity.getDefaultMap();
        if (nfc_mode) {
            map.put("tmprfid", qrcode);
        } else {
            map.put("boxno", qrcode);
        }
        ConnectionUtil.postParams(Constants.HOST + Constants.CHECK_BOX_STATUS, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage(getString(R.string.check_box_status_load));
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
                    Utils.showLongToast(returnObject.getsMsg(), getContext());
                    return;
                }
                JsonObject jsonObject = returnObject.getM_ReturnOBJJsonObject();
                String option = jsonObject.get("option").getAsString();
                JSONModel.Box box;
                if (jsonObject.has("box")) {
                    box = gson.fromJson(jsonObject.get("box"), JSONModel.Box.class);
                } else {
                    box = new JSONModel.Box();
                    box.setBoxno(qrcode);
                }
                JSONModel.TempLink tempLink = null;
                if (jsonObject.has("boxlink")) {
                    tempLink = gson.fromJson(jsonObject.get("boxlink"), JSONModel.TempLink.class);
                }

                if (TextUtils.equals("unBind", option)) {
                    unbindTag(tempLink);
                    return;
                }
                if (TextUtils.equals(ManageActivity.OPTION_NEW_BOX, option)) {
                    if (!baseActivity.userInfo.isHaveAddBox()) {
                        Utils.showLongToast(getString(R.string.no_power_to_add_box), getContext());
                        return;
                    }
                }

                Intent intent = new Intent(getContext(), ManageActivity.class);
                intent.putExtra(ManageActivity.BOX_EXTRA, box);
                intent.putExtra(ManageActivity.OPTION_EXTRA, option);
                if (tempLink != null) {
                    intent.putExtra(ManageActivity.TMP_LINK_EXTRA, tempLink);
                }
                startActivity(intent);
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, getContext());
            }

            @Override
            public void onLost() {
                baseActivity.loginAgain();
            }


        });
    }

    private void unbindTag(JSONModel.TempLink tempLink) {
        if (tempLink == null) {
            return;
        }
        if (tempLink.getRfid().indexOf(":") == -1) {
            if (mNfcAdapter == null) {
                Utils.showLongToast(getString(R.string.unSupport_nfc), mContext);
                return;
            }
            Intent intent_nfc = new Intent(getContext(), NfcActivity.class);
            intent_nfc.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_UNBIND);
            startActivity(intent_nfc);
        } else {
            Intent intent_ble = new Intent(getContext(), DeviceListActivity.class);
            intent_ble.putExtra(DeviceListActivity.BLE_MANAGE, DeviceListActivity.BLE_UNBIND);
            startActivity(intent_ble);
        }
    }
}
