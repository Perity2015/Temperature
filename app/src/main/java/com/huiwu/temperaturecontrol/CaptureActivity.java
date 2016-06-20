package com.huiwu.temperaturecontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import com.huiwu.qrcode.BaseCaptureActivity;
import com.huiwu.qrcode.view.ViewfinderView;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bluetooth.DeviceListActivity;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CaptureActivity extends BaseCaptureActivity {

    private final int REQUEST_NFC = 101;

    public static final String NFC_MODE = "NFC_MODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_qrcode_back);
        getSupportActionBar().setTitle("");

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_qrcode, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_nfc) {
            showUidDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUidDialog() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Intent intent_ble = new Intent(CaptureActivity.this, DeviceListActivity.class);
            intent_ble.putExtra(DeviceListActivity.BLE_MANAGE, DeviceListActivity.BLE_READ_UID);
            startActivityForResult(intent_ble, REQUEST_NFC);
            return;
        }
        String[] items = {"蓝牙读取", "NFC读取"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent_ble = new Intent(CaptureActivity.this, DeviceListActivity.class);
                        intent_ble.putExtra(DeviceListActivity.BLE_MANAGE, DeviceListActivity.BLE_READ_UID);
                        startActivityForResult(intent_ble, REQUEST_NFC);
                        break;
                    case 1:
                        Intent intent_nfc = new Intent(CaptureActivity.this, NfcActivity.class);
                        intent_nfc.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_READ_UID);
                        startActivityForResult(intent_nfc, REQUEST_NFC);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NFC && resultCode == RESULT_OK) {
            String rfid = data.getStringExtra(Constants.READ_UID);
            setResult(RESULT_OK, getIntent().putExtra(RESULT, rfid).putExtra(NFC_MODE, true));
            finish();
        }
    }
}
