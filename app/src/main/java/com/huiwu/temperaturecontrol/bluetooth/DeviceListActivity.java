package com.huiwu.temperaturecontrol.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.BluetoothBaseActivity;
import com.huiwu.temperaturecontrol.ChartActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

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
    public static final int BLE_UNBIND = 3;
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

    /**
     * 配置页面发送的 封装TagInfo 或 用于组装采集 信息
     */
    private JSONModel.TagInfo tagInfo;

    private final int gather_config_info = 1;
    private final int gather_error = 2;
    private final int gather_temp_info = 3;
    private final int gather_success = 4;
    private final int send_config_info = 5;
    private final int send_config_info_error = 6;
    private final int send_config_info_success = 7;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case gather_error:
                    progressDialog.dismiss();
                    Utils.showLongToast("获取信息失败", mContext);
                    break;
                case gather_config_info:
                    break;
                case gather_temp_info:
                    progressDialog.setMessage((String) msg.obj);
                    break;
                case gather_success:
                    progressDialog.dismiss();
                    sqLiteManage.insertRecords(tagInfo);
                    Intent intent = new Intent(mContext, ChartActivity.class);
                    intent.putExtra(Constants.tag_info, tagInfo);
                    startActivity(intent);
                    break;
                case send_config_info:
                    progressDialog.setMessage((String) msg.obj);
                    progressDialog.show();
                    break;
                case send_config_info_success:
                    sqLiteManage.insertRecords(tagInfo);
                    progressDialog.dismiss();
                    Utils.showLongToast((String) msg.obj, mContext);
                    bindTag();
                    break;
                case send_config_info_error:
                    progressDialog.dismiss();
                    Utils.showLongToast((String) msg.obj, mContext);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);

        progressDialog.setCancelable(false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });

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

        bleManageState = getIntent().getIntExtra(BLE_MANAGE, BLE_GATHER);
        tagInfo = getIntent().getParcelableExtra(Constants.tag_info);

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
                                if (tagInfo == null) {
                                    tagInfo = new JSONModel.TagInfo();
                                }
                                tagInfo.setUid(mDevice.getAddress());
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
//                                return;
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

                            if (L2_data_state != L2_data_send_config_info && L2_data_length == 0) {
                                Message message = new Message();
                                message.what = gather_error;
                                mHandler.sendMessage(message);
                            }
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
                Message message = new Message();
                message.what = gather_temp_info;
                message.obj = "获取温湿度信息" + L2_data_length_received * 100 / L2_data_length + "%";
                mHandler.sendMessage(message);
                parseTempInfoBytes(bytes);
                break;
            case L2_data_send_config_info:
                return;
            default:
                return;
        }
        L2_data_length_received += bytes.length;
        Log.d(TAG, String.valueOf(L2_data_length_received));
        if (L2_data_length_received == L2_data_length) {
            switch (L2_data_state) {
                case L2_date_config_info:
                    parseConfigInfoBytes(L2_data);
                    break;
                case L2_data_temp_info:
                    Message message = new Message();
                    message.what = gather_success;
                    mHandler.sendMessage(message);
                    break;
                case L2_data_send_config_info:
                    return;
                default:
                    return;
            }
        } else {
//            mService.writeRXCharacteristic(getSendBytes(0x05));
        }
    }

    private void parseConfigInfoBytes(byte[] bytes) {
        Log.d(TAG, "START PARSE");
        if (bytes[0] != 0x01 || bytes[2] != 0x04) {
            Message message = new Message();
            message.what = gather_error;
            message.obj = "解析配置信息错误";
            mHandler.sendMessage(message);
            return;
        }
        int remark_length = bytes[34];
        byte[] remark_bytes = new byte[remark_length];
        System.arraycopy(bytes, 35, remark_bytes, 0, remark_length);
        String remark = new String(remark_bytes, Charset.forName("GB2312"));
        TLog.d(TAG, remark);

        int company_length = bytes[34 + remark_length + 1];
        byte[] company_bytes = new byte[company_length];
        System.arraycopy(bytes, 34 + remark_length + 1 + 1, company_bytes, 0, company_length);
        String company = new String(company_bytes, Charset.forName("GB2312"));

        tagInfo.setLinkuuid(company);
        TLog.d(TAG, company);

        int goodType_length = bytes[34 + remark_length + 1 + company_length + 1];
        byte[] goodsType_bytes = new byte[goodType_length];
        System.arraycopy(bytes, 34 + remark_length + 1 + company_length + 1 + 1, goodsType_bytes, 0, goodType_length);
        String goodType = new String(goodsType_bytes, Charset.forName("GB2312"));

        Log.d(TAG, goodType);

        int place_length = bytes[34 + remark_length + 1 + company_length + 1 + goodType_length + 1];
        byte[] place_bytes = new byte[place_length];
        System.arraycopy(bytes, 34 + remark_length + 1 + company_length + 1 + goodType_length + 1 + 1, place_bytes, 0, place_length);
        String place = new String(place_bytes, Charset.forName("GB2312"));
        Log.d(TAG, place);

        int back_length = bytes[34 + remark_length + 1 + company_length + 1 + goodType_length + 1 + place_length + 1];
        byte[] back_bytes = new byte[back_length];
        System.arraycopy(bytes, 34 + remark_length + 1 + company_length + 1 + goodType_length + 1 + place_length + 1 + 1, back_bytes, 0, back_length);
        String back = new String(back_bytes, Charset.forName("GB2312"));
        Log.d(TAG, back);

        tagInfo = sqLiteManage.getLastTagInfo(tagInfo.getLinkuuid(),tagInfo.getUid());

        byte[] systemStatus = BluetoothUtil.byteToBitBytes(bytes[6]);
        if (systemStatus[7] == 0x01 && systemStatus[6] == 0x00) {
            tagInfo.setJustTemp(false);
        } else if (systemStatus[7] == 0x00 && systemStatus[6] == 0x01) {
            tagInfo.setJustTemp(true);
        }

        tagInfo.setIndex(BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[7], bytes[8]}));
        if (tagInfo.getIndex() == 0){
            Message message = new Message();
            message.what = gather_error;
            message.obj = "没有记录信息";
            mHandler.sendMessage(message);
            return;
        }

        tagInfo.setPower(bytes[9] / 20);
        Calendar calendar = Calendar.getInstance();
        tagInfo.setReadTime(calendar.getTimeInMillis());

        calendar.set(Calendar.YEAR, 2000 + bytes[14]);
        calendar.set(Calendar.MONTH, bytes[15] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, bytes[16]);
        calendar.set(Calendar.HOUR_OF_DAY, bytes[17]);
        calendar.set(Calendar.MINUTE, bytes[18]);
        calendar.set(Calendar.SECOND, bytes[19]);
        calendar.set(Calendar.MILLISECOND, 0);
        tagInfo.setEndTime(calendar.getTimeInMillis());

        JSONModel.Goods goods = new JSONModel.Goods();
        goods.setHightmpnumber(bytes[20]);
        goods.setLowtmpnumber(bytes[21]);
        goods.setHighhumiditynumber(bytes[22]);
        goods.setLowhumiditynumber(bytes[23]);
        goods.setOnetime(BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[24], bytes[25]}));
        tagInfo.setGoods(goods);

        long startTime = tagInfo.getEndTime() - (tagInfo.getIndex() - 1) * goods.getOnetime() * 60 * 1000L;
        if (startTime - tagInfo.getStartTime() > goods.getOnetime()*60*1000L){
            tagInfo.setStartTime(startTime);
        }

        L2_data_state = L2_data_temp_info;
        mService.writeRXCharacteristic(getSendBytes(0x02));
    }

    private void parseTempInfoBytes(byte[] bytes) {
        if (!tagInfo.isJustTemp()) {
            for (int i = 0; i < bytes.length / 4; i++) {
                double temp = BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[i * 4], bytes[i * 4 + 1]}) / 100.00D;
                double hum = BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[i * 4 + 2], bytes[i * 4 + 3]});
                checkIsOutLimit(temp, hum);
            }
        } else {
            for (int i = 0; i < bytes.length / 2; i++) {
                double temp = BluetoothUtil.Convert2bytesHexFormatToInt(new byte[]{bytes[i * 2], bytes[i * 2 + 1]}) / 100.00D;
                double hum = temp;
                checkIsOutLimit(temp, hum);
            }
        }
    }

    private void checkIsOutLimit(double temp, double hum) {
//        if (temp == 2.55 || hum == 255){
//            return;
//        }
        tagInfo.getTempList().add(temp);
        tagInfo.getHumList().add(hum);
        if (tagInfo.getHum_max() == 0 && tagInfo.getTemp_max() == 0) {
            tagInfo.setTemp_max(temp);
            tagInfo.setTemp_min(temp);
            tagInfo.setHum_max(hum);
            tagInfo.setHum_min(hum);
        }
        tagInfo.setTemp_min(Math.min(tagInfo.getTemp_min(), temp));
        tagInfo.setTemp_max(Math.max(tagInfo.getTemp_max(), temp));
        tagInfo.setHum_max(Math.max(tagInfo.getHum_max(), hum));
        tagInfo.setHum_min(Math.min(tagInfo.getHum_min(), hum));
        if (tagInfo.isOutLimit()) {
            return;
        }
        if (tagInfo.getGoods().getLowtmpnumber() > temp
                || tagInfo.getGoods().getHightmpnumber() < temp
                || tagInfo.getGoods().getLowhumiditynumber() > hum
                || tagInfo.getGoods().getHighhumiditynumber() < hum) {
            tagInfo.setOutLimit(true);
        }
    }

    /**
     * 获取发送字节
     *
     * @param key 0x03 返回配置信息 0x02 返回温湿度信息0x05 返回更多信息
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

    private byte[] getConfigBytes(JSONModel.TagInfo tagInfo) throws Exception {
        sequence_id += 1;

        JSONModel.Goods goods = tagInfo.getGoods();
        byte[] configValues_1 = {-1, -1, -127,
                (byte) goods.getHightmpnumber(), (byte) goods.getLowtmpnumber(), (byte) goods.getHighhumiditynumber(), (byte) goods.getLowhumiditynumber(),
                0x00, 0x01,
                0x00, 0x01
        };

        byte[] configValues = new byte[configValues_1.length + 6];
        System.arraycopy(configValues_1, 0, configValues, 0, configValues_1.length);
        Calendar calendar = Calendar.getInstance();
        configValues[configValues_1.length] = (byte) (calendar.get(Calendar.YEAR) % 100);
        configValues[configValues_1.length + 1] = (byte) (calendar.get(Calendar.MONTH) + 1);
        configValues[configValues_1.length + 2] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        configValues[configValues_1.length + 3] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        configValues[configValues_1.length + 4] = (byte) calendar.get(Calendar.MINUTE);
        configValues[configValues_1.length + 5] = (byte) calendar.get(Calendar.SECOND);

        byte[] UTF = "test".getBytes("GB2312");
        byte[] UTF_1 = new byte[UTF.length + 1];
        UTF_1[0] = (byte) UTF.length;
        System.arraycopy(UTF, 0, UTF_1, 1, UTF.length);

        String linkuuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        tagInfo.setLinkuuid(linkuuid);
        byte[] linkuuidBytes = linkuuid.getBytes("GB2312");
        byte[] linkuuidBytes_1 = new byte[linkuuidBytes.length + 1];
        linkuuidBytes_1[0] = (byte) linkuuidBytes.length;
        System.arraycopy(linkuuidBytes, 0, linkuuidBytes_1, 1, linkuuidBytes.length);

        byte[] boxidBytes = String.valueOf(tagInfo.getBox().getBoxid()).getBytes("GB2312");
        byte[] boxidBytes_1 = new byte[boxidBytes.length + 1];
        boxidBytes_1[0] = (byte) boxidBytes.length;
        System.arraycopy(boxidBytes, 0, boxidBytes_1, 1, boxidBytes.length);

        byte[] goodsIdBytes = String.valueOf(tagInfo.getGoods().getId()).getBytes("GB2312");
        byte[] goodsIdBytes_1 = new byte[goodsIdBytes.length + 1];
        goodsIdBytes_1[0] = (byte) goodsIdBytes.length;
        System.arraycopy(goodsIdBytes, 0, goodsIdBytes_1, 1, goodsIdBytes.length);

        byte[] back = "慧物".getBytes("GB2312");
        byte[] back_1 = new byte[back.length + 1];
        back_1[0] = (byte) back.length;
        System.arraycopy(back, 0, back_1, 1, back.length);

        byte[] keyValues = new byte[UTF_1.length + linkuuidBytes_1.length + boxidBytes_1.length + goodsIdBytes_1.length + back_1.length + configValues.length];
        System.arraycopy(configValues, 0, keyValues, 0, configValues.length);

        System.arraycopy(UTF_1, 0, keyValues, configValues.length, UTF_1.length);

        System.arraycopy(linkuuidBytes_1, 0, keyValues, configValues.length + UTF_1.length, linkuuidBytes_1.length);

        System.arraycopy(boxidBytes_1, 0, keyValues, configValues.length + UTF_1.length + linkuuidBytes_1.length, boxidBytes_1.length);

        System.arraycopy(goodsIdBytes_1, 0, keyValues, configValues.length + UTF_1.length + linkuuidBytes_1.length + boxidBytes_1.length, goodsIdBytes_1.length);

        System.arraycopy(back_1, 0, keyValues, configValues.length + UTF_1.length + linkuuidBytes_1.length + boxidBytes_1.length + goodsIdBytes_1.length, back_1.length);

        byte[] L2 = new byte[5 + keyValues.length];
        L2[0] = 0x02;
        L2[1] = 0x00;
        L2[2] = 0x01;
        L2[3] = (byte) (keyValues.length / 256);
        L2[4] = (byte) (0xFF & (byte) keyValues.length);
        System.arraycopy(keyValues, 0, L2, 5, keyValues.length);

        byte[] L1 = new byte[8 + L2.length];
        L1[0] = (byte) 0xAB;
        L1[1] = 0x00;
        L1[2] = (byte) (L2.length / 256);
        L1[3] = (byte) (0xFF & (byte) L2.length);

        int i = BluetoothUtil.crcTable(L2);
        L1[4] = (byte) (i / 256);
        L1[5] = (byte) (0xFF & (byte) i);

        L1[6] = (byte) (sequence_id / 256);
        L1[7] = (byte) (0xFF & (byte) sequence_id);

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
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
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
                    if (mScanning == true)
                        scanLeDevice(false);
                    if (bleManageState == BLE_UNBIND) {
                        tagInfo = new JSONModel.TagInfo();
                        tagInfo.setUid(bleTag.getAddress());
                        unBindTag();
                        return;
                    }
                    if (mDevice != null) {
                        return;
                    }
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bleTag.getAddress());
                    progressDialog.setMessage("连接蓝牙设备中……");
                    progressDialog.show();
                    mService.connect(mDevice.getAddress());
                }
            });

            viewHolder.btnDeviceGather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!bleTag.isConfig_status()) {
                        Utils.showLongToast("没有配置信息,无法采集记录", mContext);
                        return;
                    }
                    progressDialog.setMessage("获取配置信息中……");
                    progressDialog.show();
                    mService.writeRXCharacteristic(getSendBytes(0x03));
                }
            });

            viewHolder.btnDeviceConfig.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bleTag.isConfig_status() && bleTag.isRecord_status()) {
                        Utils.showLongToast("已配置,请停止记录后再进行配置", mContext);
                        return;
                    }

                    try {
                        sendConfigInfoBytes();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message message = new Message();
                        message.what = send_config_info_error;
                        message.obj = "发送配置信息失败";
                        mHandler.sendMessage(message);
                    }
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

    private void sendConfigInfoBytes() throws Exception {
        L2_data_state = L2_data_send_config_info;
        L1_data = getConfigBytes(tagInfo);
        L1_data_length = L1_data.length;
        write_config_info_success = true;
        Message message = new Message();
        message.what = send_config_info;
        message.obj = "发送配置信息中";
        mHandler.sendMessage(message);

        new Thread() {
            @Override
            public void run() {
                super.run();
                while (L1_data_length_sent < L1_data_length) {
                    if (write_config_info_success) {
                        write_config_info_success = false;
                        int num = L1_data_length - L1_data_length_sent;

                        byte[] bytes = new byte[num > 20 ? 20 : num];
                        System.arraycopy(L1_data, L1_data_length_sent, bytes, 0, num > 20 ? 20 : num);
                        Log.d(TAG, Arrays.toString(bytes));

                        mService.writeRXCharacteristic(bytes);

                        L1_data_length_sent += 20;
                    }
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Message message = new Message();
                message.what = send_config_info_success;
                message.obj = "配置成功";
                mHandler.sendMessage(message);
            }
        }.start();
    }

    private void bindTag() {
        JSONModel.Box box = tagInfo.getBox();
        JSONModel.Goods goods = tagInfo.getGoods();

        final HashMap<String, String> map = getDefaultMap();
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("boxno", box.getBoxno());
        map.put("boxtype", box.getBoxtype());
        map.put("tmptype", "normal");
        if (mainApp.bdLocation != null) {
            map.put("lat", String.valueOf(mainApp.bdLocation.getLatitude()));
            map.put("lng", String.valueOf(mainApp.bdLocation.getLongitude()));
            map.put("beginaddr", mainApp.bdLocation.getAddrStr());
        } else {
            map.put("beginaddr", "未获取定位信息");
        }

        map.put("goodtype", goods.getParentgoodtype());
        map.put("goodchildtype", goods.getGoodtype());
        map.put("goodtypeid", String.valueOf(goods.getId()));
        map.put("carno", tagInfo.getObject());
        map.put("hightmpnumber", String.valueOf(goods.getHightmpnumber()));
        map.put("lowtmpnumber", String.valueOf(goods.getLowtmpnumber()));
        map.put("highhumiditynumber", String.valueOf(goods.getHighhumiditynumber()));
        map.put("lowhumiditynumber", String.valueOf(goods.getLowhumiditynumber()));
        map.put("onetime", String.valueOf(goods.getOnetime()));
        map.put("actrealname", userInfo.getRealname());
        map.put("actuser", userInfo.getUsername());
        map.put("boxid", String.valueOf(box.getBoxid()));
        map.put("rfid", tagInfo.getUid());
        map.put("createtime", Utils.formatDateTimeOffLine(System.currentTimeMillis()));
        cancelConnectionTask();
        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage(getString(R.string.config_data_post_load));
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
//                Utils.showLongToast(result, mContext);
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    if (returnObject.getM_ReturnOBJJsonObject().has("isUse") && returnObject.getM_ReturnOBJJsonObject().get("isUse").getAsBoolean()) {
                        showUnbindDialog(returnObject.getsMsg());
                    }
                    Utils.showLongToast(returnObject.getsMsg(), mContext);
                    return;
                }
                sqLiteManage.insertFirstTagInfo(tagInfo);
                Intent intent = new Intent();
                intent.putExtra(Constants.tag_info,tagInfo);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void sendLost(String result) {
                loginAgain();
            }
        });
        task.execute(Constants.bind_tag_offLine_url);
    }

    private void showUnbindDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.notice);
        builder.setMessage(message + getString(R.string.confirm_unbind_notice));
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                unBindTag();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void unBindTag() {
        cancelConnectionTask();
        HashMap<String, String> map = getDefaultMap();
        map.put("rfid", tagInfo.getUid());
        if (mainApp.bdLocation != null) {
            map.put("lat", String.valueOf(mainApp.bdLocation.getLatitude()));
            map.put("lng", String.valueOf(mainApp.bdLocation.getLongitude()));
            map.put("endaddr", mainApp.bdLocation.getAddrStr());
        } else {
            map.put("endaddr", "未获取定位信息");
        }
        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage(getString(R.string.unbind_post_load));
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
                if (returnObject.isbOK()) {
                    Utils.showLongToast(R.string.unbind_tag_success_to_continue, mContext);
                    if (bleManageState == BLE_UNBIND) {
                        finish();
                        return;
                    }
                    bindTag();
                    return;
                }
                Utils.showLongToast(returnObject.getsMsg(), mContext);
            }

            @Override
            public void sendLost(String result) {
                loginAgain();
            }
        });
        task.execute(Constants.unbind_tag_url);
    }
}
