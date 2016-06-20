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
    private static final String EXTRA_PARAM1 = "com.huiwu.temperaturecontrol.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.huiwu.temperaturecontrol.service.extra.PARAM2";

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
    public static void startActionNow(Context context, Bundle bundle) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_NOW);
        intent.putExtra(EXTRA_PARAM1, bundle);
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
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOW.equals(action)) {
                final Bundle bundle = intent.getBundleExtra(EXTRA_PARAM1);
                try {
                    handleActionNow(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ACTION_SYNC.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNow(Bundle bundle) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, File> fileHashMap = new HashMap<>();
        for (String s : bundle.keySet()) {
            if (TextUtils.equals("file", s)) {
                File file = new File((String) bundle.get(s));
                fileHashMap.put("file", file);
            } else {
                map.put(s, (String) bundle.get(s));
            }
        }
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
