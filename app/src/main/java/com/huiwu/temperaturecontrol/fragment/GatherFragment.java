package com.huiwu.temperaturecontrol.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bluetooth.DeviceListActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class GatherFragment extends Fragment {

    @Bind(R.id.btn_bluetooth)
    ImageView btnBluetooth;
    @Bind(R.id.btn_nfc)
    ImageView btnNfc;
    @Bind(R.id.image_unbind)
    ImageView imageUnbind;

    public GatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gather, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.btn_bluetooth, R.id.btn_nfc, R.id.image_unbind})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bluetooth:
                startActivity(new Intent(getContext(), DeviceListActivity.class));
                break;
            case R.id.btn_nfc:
                startActivity(new Intent(getContext(), NfcActivity.class));
                break;
            case R.id.image_unbind:
                Intent intent = new Intent(getContext(), NfcActivity.class);
                intent.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_UNBIND);
                startActivity(intent);
                break;
        }
    }
}
