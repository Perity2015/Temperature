package com.huiwu.temperaturecontrol.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.TestLog;

import java.io.File;
import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SyncService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_NOW = "com.huiwu.temperaturecontrol.service.action.NOW";
    private static final String ACTION_SYNC = "com.huiwu.temperaturecontrol.service.action.SYNC";

    // TODO: Rename parameters
    private static final String EXTRA_MAP = "com.huiwu.temperaturecontrol.service.extra.EXTRA_MAP";
    private static final String EXTRA_FILE = "com.huiwu.temperaturecontrol.service.extra.EXTRA_FILE";

    public SyncService() {
        super("SyncService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionNow(Context context, HashMap<String, String> map, HashMap<String, File> fileHashMap) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_NOW);
        intent.putExtra(EXTRA_MAP, map);
        intent.putExtra(EXTRA_FILE, fileHashMap);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSync(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
        intent.putExtra(EXTRA_MAP, param1);
        intent.putExtra(EXTRA_FILE, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOW.equals(action)) {
                final HashMap<String, String> map = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_MAP);
                final HashMap<String, File> fileHashMap = (HashMap<String, File>) intent.getSerializableExtra(EXTRA_FILE);
                try {
                    handleActionNow(map, fileHashMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ACTION_SYNC.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_MAP);
                final String param2 = intent.getStringExtra(EXTRA_FILE);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNow(HashMap<String, String> map, HashMap<String, File> fileHashMap) throws Exception {
        String result = ConnectionUtil.getResponse(Constants.UPLOAD_PICTURE, map, fileHashMap);
        TestLog.d("DEBUG", result);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
