package com.huiwu.temperaturecontrol.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.huiwu.temperaturecontrol.BluetoothBaseActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.JSONModel;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceListActivity extends BluetoothBaseActivity {
    @Bind(R.id.listView_devices)
    ListView listViewDevices;
    @Bind(R.id.btn_scan)
    Button btnScan;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    private String TAG = DeviceListActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;

    private ArrayAdapter deviceAdapter;

    private static final long SCAN_PERIOD = 10000; //10 seconds

    private Handler mHandler;

    private boolean mScanning;

    private ArrayList<JSONModel.BLETag> bleTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_back);

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bleTags = new ArrayList<>();
        deviceAdapter = new ArrayAdapter(this, R.layout.layout_device_item, bleTags);

        listViewDevices.setAdapter(deviceAdapter);
        listViewDevices.setOnItemClickListener(mDeviceClickListener);

        scanLeDevice(true);
    }

    @OnClick(R.id.btn_scan)
    public void onClick() {
        scanLeDevice(!mScanning);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    btnScan.setText(R.string.scan_device);
                    progressBar.setVisibility(View.INVISIBLE);

                }
            }, SCAN_PERIOD);

            mScanning = true;

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            btnScan.setText(R.string.scaning);

            progressBar.setVisibility(View.VISIBLE);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            btnScan.setText(R.string.scan_device);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {


                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, Arrays.toString(scanRecord));

                            //0xFF,0xFF,0xFF 标识符
                            if (scanRecord[4] == (byte) 0xFF && scanRecord[5] == (byte) 0xFF && scanRecord[6] == (byte) 0xFF)
                                addDevice(device, rssi, scanRecord);
                        }
                    });
                }
            };

    private void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        JSONModel.BLETag bleTag = new JSONModel.BLETag(device, rssi, scanRecord);

        if (!bleTags.contains(bleTag)) {
            bleTags.add(bleTag);
        }else {
            for (int i = 0; i < bleTags.size(); i++) {
                if (bleTags.get(i).getAddress().equals(bleTag.getAddress())){
                    bleTags.set(i,bleTag);
                    break;
                }
            }
        }
        deviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            JSONModel.BLETag bleTag = bleTags.get(position);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, bleTag.getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();

        }
    };


    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }


}
