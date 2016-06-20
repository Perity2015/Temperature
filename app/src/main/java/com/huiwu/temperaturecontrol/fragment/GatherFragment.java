package com.huiwu.temperaturecontrol.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bluetooth.DeviceListActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class GatherFragment extends BaseFragment {

    @Bind(R.id.btn_bluetooth)
    ImageView btnBluetooth;
    @Bind(R.id.btn_nfc)
    ImageView btnNfc;

    public GatherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mNfcAdapter == null) {
            btnNfc.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setIcon(R.drawable.image_unbind);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getContext(), NfcActivity.class);
                intent.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_UNBIND);
                startActivity(intent);
                return true;
            }
        });
    }


    @OnClick({R.id.btn_bluetooth, R.id.btn_nfc})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bluetooth:
                Intent intent_ble = new Intent(getContext(), DeviceListActivity.class);
                intent_ble.putExtra(DeviceListActivity.BLE_MANAGE, DeviceListActivity.BLE_GATHER);
                startActivity(intent_ble);
                break;
            case R.id.btn_nfc:
                startActivity(new Intent(getContext(), NfcActivity.class));
                break;
        }
    }
}
