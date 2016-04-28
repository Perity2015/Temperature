package com.huiwu.temperaturecontrol;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.Toast;

public class NfcBaseActivity extends BaseActivity {
    public NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            Toast.makeText(this, getString(R.string.unSupport_nfc), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mAdapter.isEnabled()) {
                Toast.makeText(this, getString(R.string.please_open_nfc), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                mFilters = new IntentFilter[]{ndef};
                mTechLists = new String[][]{new String[]{android.nfc.tech.NfcV.class.getName()}, new String[]{android.nfc.tech.NfcA.class.getName()}};
            }
        }
    }

    @Override
    protected void onResume() {
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mAdapter.disableForegroundDispatch(this);
        super.onPause();
    }
}
