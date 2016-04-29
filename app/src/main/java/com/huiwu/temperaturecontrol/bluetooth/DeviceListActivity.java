package com.huiwu.temperaturecontrol.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.BluetoothBaseActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceListActivity extends BluetoothBaseActivity {
    @Bind(R.id.recyclerView_devices)
    RecyclerView recyclerViewDevices;
    @Bind(R.id.btn_scan)
    Button btnScan;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    public static String TAG = DeviceListActivity.class.getSimpleName();

    private ServiceConnection mServiceConnection;

    private BroadcastReceiver UARTStatusChangeReceiver;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothService mService = null;

    private BluetoothDevice mDevice = null;

    private DeviceAdapter deviceAdapter;

    private static final long SCAN_PERIOD = 10000; //10 seconds

    private boolean mScanning;

    private ArrayList<JSONModel.BLETag> bleTags;
    /**
     * 蓝牙操作 配置信息 读取信息
     */
    public static final String BLE_MANAGE = "BLE_MANAGE";
    public static final int BLE_CONFIG = 1;
    public static final int BLE_GATHER = 2;
    public int bleManageState = BLE_GATHER;

    /**
     * 服务连接状态
     */
    private int mState = UART_PROFILE_DISCONNECTED;
    private static final int UART_PROFILE_CONNECTED = 1;
    private static final int UART_PROFILE_DISCONNECTED = 2;

    /**
     * 发送指令是否成功 用于多字节分批次发送
     */
    private boolean write_config_info_success = true;

    /**
     * 选择操作的蓝牙设备的序号
     */
    private int select_position = -1;

    /**
     * 蓝牙指令序号 用于区别是否为此次请求
     */
    private int sequence_id = 100;

    /**
     * 需要接收的信息L2包的长度
     */
    private int L2_data_length = 0;
    /**
     * 已经接收到的L2包的长度
     */
    private int L2_data_length_received = 0;
    /**
     * 用于接收L2_data的数组
     */
    private byte[] L2_data;
    /**
     * 用于发送的L1_data数组
     */
    private byte[] L1_data;
    /**
     * 需要发送的L1包的长度
     */
    private int L1_data_length = 0;
    /**
     * 已经发送的L1包的长度
     */
    private int L1_data_length_sent = 0;
    /**
     * 返回的L2data属于配置信息 或者 温湿度信息
     */
    private int L2_data_state;
    private final int L2_date_config_info = 1;
    private final int L2_data_temp_info = 2;
    private final int L2_data_send_config_info = 3;
    private final int L2_data_default = 0;

    /**
     * 配置页面发送的 封装TagInfo 或 用于组装采集 信息
     */
    private JSONModel.TagInfo tagInfo;

    private final int gather_error = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


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
        } else if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        initData();

        recyclerViewDevices.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        bleTags = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(bleTags);

        recyclerViewDevices.setAdapter(deviceAdapter);

        scanLeDevice(true);
    }

    private void initData() {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

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

        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BluetoothService.LocalBinder) rawBinder).getService();
                Log.d(TAG, "onServiceConnected mService= " + mService);
                if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
            }

            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
            }
        };

        UARTStatusChangeReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BluetoothService.ACTION_GATT_CONNECTED:
                        Log.d(TAG, "UART_CONNECT_MSG");
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        for (JSONModel.BLETag bleTag : bleTags) {
                            if (bleTag.getAddress().equals(mDevice.getAddress())) {
                                select_position = 0;
                                bleTags.clear();
                                bleTags.add(bleTag);
                                deviceAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                        Utils.showLongToast("连接成功", mContext);
                        mState = UART_PROFILE_CONNECTED;
                        break;
                    case BluetoothService.ACTION_GATT_DISCONNECTED:
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        mDevice = null;
                        select_position = -1;
                        deviceAdapter.notifyDataSetChanged();
                        Utils.showLongToast("断开连接", mContext);
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        break;
                    case BluetoothService.ACTION_GATT_WRITE_SUCCESSED:
                        Log.d(TAG, "WRITE_CONFIG_SUCCESS");
                        write_config_info_success = true;
                        break;
                    case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                        TLog.d(TAG, "GATT_SERVICES_DISCOVERED");
                        mService.enableTXNotification(true);
                        break;
                    case BluetoothService.ACTION_DATA_AVAILABLE:
                        TLog.d(TAG, "DATA_AVAILABLE");
                        final byte[] receiverBytes = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
                        TLog.d(TAG, Arrays.toString(receiverBytes));
                        if (receiverBytes[0] == (byte) 0xAB && receiverBytes.length == 8) {
                            int temp_sequence_id = BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{receiverBytes[6], receiverBytes[7]});
                            if (temp_sequence_id != sequence_id) {
                                sequence_id = temp_sequence_id;
//                                mService.disconnect();
                                return;
                            }
                            /**
                             * 用于判断发送指令 返回状态
                             */
                            byte[] manageStatusBytes = BluetoothUtil.byteToBitBytes(receiverBytes[1]);
                            if (manageStatusBytes[5] != 0x00) {
                                mService.disconnect();
                                return;
                            }
                            L2_data_length = BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{receiverBytes[2], receiverBytes[3]});
                            L2_data_length_received = 0;
                            L2_data = new byte[L2_data_length];
                        } else {
                            try {
                                parseAvailableData(receiverBytes);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART:
                        TLog.d(TAG, "Device doesn't support UART. Disconnecting");
                        mService.disconnect();
                        break;
                }
            }
        };

        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(BluetoothService.ACTION_GATT_WRITE_SUCCESSED);
        return intentFilter;
    }

    private void parseAvailableData(byte[] bytes) throws Exception {
        switch (L2_data_state) {
            case L2_date_config_info:
                System.arraycopy(bytes, 0, L2_data, L2_data_length_received, bytes.length);
                break;
            case L2_data_temp_info:
                break;
            case L2_data_send_config_info:
                return;
            default:
                return;
        }
        L2_data_length_received += bytes.length;
        if (L2_data_length_received == L2_data_length) {
            switch (L2_data_state) {
                case L2_date_config_info:
                    parseConfigInfoBytes(L2_data);
                    break;
                case L2_data_temp_info:
                    break;
                case L2_data_send_config_info:
                    return;
                default:
                    return;
            }
        }
    }

    private void parseConfigInfoBytes(byte[] bytes) {
        if (bytes[0] != 0x01 || bytes[2] != 0x04) {
            Message message = new Message();
            message.what = gather_error;
            message.obj = "解析配置信息错误";
            mHandler.sendMessage(message);
            return;
        }
        tagInfo = new JSONModel.TagInfo();
        byte[] systemStatus = BluetoothUtil.byteToBitBytes(bytes[6]);
        if (systemStatus[7] == 0x01 && systemStatus[6] == 0x00) {
            tagInfo.setJustTemp(false);
        } else if (systemStatus[7] == 0x00 && systemStatus[6] == 0x01) {
            tagInfo.setJustTemp(true);
        }

        tagInfo.setIndex(BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[7], bytes[8]}));
        tagInfo.setPower(bytes[9] / 20);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2000 + bytes[14]);
        calendar.set(Calendar.MONTH, bytes[15] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, bytes[16]);
        calendar.set(Calendar.HOUR_OF_DAY, bytes[17]);
        calendar.set(Calendar.MINUTE, bytes[18]);
        calendar.set(Calendar.SECOND, bytes[19]);
        tagInfo.setStartTime(calendar.getTimeInMillis());

        JSONModel.Goods goods = new JSONModel.Goods();
        goods.setHightmpnumber(bytes[20]);
        goods.setLowtmpnumber(bytes[21]);
        goods.setHighhumiditynumber(bytes[22]);
        goods.setLowhumiditynumber(bytes[23]);
        goods.setOnetime(BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[24], bytes[25]}));
        tagInfo.setGoods(goods);

        int remark_length = bytes[26];
        byte[] remark_bytes = new byte[remark_length];
        System.arraycopy(bytes, 27, remark_bytes, 0, remark_length);
        String remark = new String(remark_bytes, Charset.forName("GB2312"));
        TLog.d(TAG, remark);

        int company_length = bytes[26 + remark_length + 1];
        byte[] company_bytes = new byte[company_length];
        System.arraycopy(bytes, 26 + remark_length + 1 + 1, company_bytes, 0, company_length);
        String company = new String(company_bytes, Charset.forName("GB2312"));
        TLog.d(TAG, company);

    }

    /**
     * 获取发送字节
     *
     * @param key 0x03 返回配置信息 0x02 返回温湿度信息
     * @return
     */
    private byte[] getSendBytes(int key) {
        if (key == 0x02) {
            L2_data_state = L2_data_temp_info;
        } else {
            L2_data_state = L2_date_config_info;
        }

        sequence_id += 1;

        byte[] L2 = new byte[5];
        L2[0] = 1;
        L2[1] = 0;
        L2[2] = (byte) key;
        L2[3] = 0;
        L2[4] = 0;

        byte[] L1 = new byte[8 + L2.length];
        L1[0] = (byte) 0xAB;
        L1[1] = 0;
        L1[2] = (byte) (L2.length / 256);
        L1[3] = (byte) (0xFF & (byte) L2.length);
        L1[6] = (byte) (sequence_id / 256);
        L1[7] = (byte) (0xFF & (byte) sequence_id);

        int i = BluetoothUtil.crcTable(L2);
        L1[4] = (byte) (i / 256);
        L1[5] = (byte) (0xFF & (byte) i);

        System.arraycopy(L2, 0, L1, 8, L2.length);
        return L1;
    }

    @OnClick(R.id.btn_scan)
    public void onClick() {
        scanLeDevice(!mScanning);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (mDevice != null) {
                mService.disconnect();
            }
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

    private void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        JSONModel.BLETag bleTag = new JSONModel.BLETag(device, rssi, scanRecord);
        if (!bleTags.contains(bleTag)) {
            bleTags.add(bleTag);
        } else {
            for (int i = 0; i < bleTags.size(); i++) {
                if (bleTags.get(i).getAddress().equals(bleTag.getAddress())) {
                    bleTags.set(i, bleTag);
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

    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mService.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private class DeviceAdapter extends RecyclerView.Adapter {
        private ArrayList<JSONModel.BLETag> bleTags;

        public DeviceAdapter(ArrayList<JSONModel.BLETag> bleTags) {
            this.bleTags = bleTags;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            DeviceViewHolder viewHolder = new DeviceViewHolder(getLayoutInflater().inflate(R.layout.layout_device_item, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            DeviceViewHolder viewHolder = (DeviceViewHolder) holder;
            final JSONModel.BLETag bleTag = bleTags.get(position);

            viewHolder.textDeviceRemark.setText(bleTag.getRemark());
            viewHolder.textDeviceStatus.setText(bleTag.getStatus());

            if (select_position == position) {
                viewHolder.imageDeviceRight.setVisibility(View.GONE);
                if (bleManageState == BLE_CONFIG) {
                    viewHolder.btnDeviceConfig.setVisibility(View.VISIBLE);
                    viewHolder.btnDeviceGather.setVisibility(View.GONE);
                } else {
                    viewHolder.btnDeviceConfig.setVisibility(View.GONE);
                    viewHolder.btnDeviceGather.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.imageDeviceRight.setVisibility(View.VISIBLE);
                viewHolder.btnDeviceConfig.setVisibility(View.GONE);
                viewHolder.btnDeviceGather.setVisibility(View.GONE);
            }

            viewHolder.layoutDeviceItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanLeDevice(false);
                    if (mDevice != null) {
                        return;
                    }
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bleTag.getAddress());
                    progressDialog.setMessage("连接蓝牙设备中……");
                    progressDialog.show();
                    mService.connect(bleTag.getAddress());
                }
            });

            viewHolder.btnDeviceGather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.setMessage("获取配置信息中……");
                    progressDialog.show();
                    mService.writeRXCharacteristic(getSendBytes(0x03));
                }
            });

        }

        @Override
        public int getItemCount() {
            return bleTags.size();
        }
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_device_remark)
        TextView textDeviceRemark;
        @Bind(R.id.text_device_status)
        TextView textDeviceStatus;
        @Bind(R.id.btn_device_config)
        Button btnDeviceConfig;
        @Bind(R.id.btn_device_gather)
        Button btnDeviceGather;
        @Bind(R.id.layout_device_item)
        CardView layoutDeviceItem;
        @Bind(R.id.image_device_right)
        ImageView imageDeviceRight;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}
