package com.huiwu.temperaturecontrol.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.temperaturecontrol.application.MainApp;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TestLog;
import com.huiwu.temperaturecontrol.sqlite.SQLiteManage;
import com.huiwu.temperaturecontrol.sqlite.bean.Picture;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
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

    private MainApp mainApp;
    private Gson gson = new Gson();
    private SharedPreferences mShared;
    private JSONModel.UserInfo userInfo;
    private SQLiteManage sqLiteManage;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();
        mainApp = (MainApp) getApplication();
        sqLiteManage = new SQLiteManage(getApplicationContext());
        mShared = context.getSharedPreferences(Constants.SHARED, MODE_PRIVATE);
        String user = mShared.getString(Constants.USER_INFO, "{}");
        try {
            userInfo = gson.fromJson(new String(Base64.decode(user.getBytes(), Base64.DEFAULT)), JSONModel.UserInfo.class);
        } catch (Exception e) {
            userInfo = null;
        }
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
    public static void startActionSync(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
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
                try {
                    handleActionSync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNow(HashMap<String, String> map, HashMap<String, File> fileHashMap) throws Exception {
        String result = ConnectionUtil.getResponse(Constants.HOST + Constants.UPLOAD_PICTURE, map, fileHashMap);
        TestLog.d("DEBUG", result);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSync() throws Exception {
        if (userInfo == null) {
            return;
        }
        ArrayList<Picture> pictures = sqLiteManage.getNotPostPictures(mainApp.getDaoSession());
        if (pictures.size() == 0) {
            return;
        }
        HashMap<String, String> hashMap = new HashMap<>();
        HashMap<String, File> fileHashMap = new HashMap<>();
        JSONModel.ReturnObject returnObject;

        for (Picture picture : pictures) {
            hashMap.clear();
            fileHashMap.clear();

            hashMap.put("boxno", picture.getBoxno());
            hashMap.put("linkuuid", picture.getLinkuuid());
            hashMap.put("sealOropen", picture.getSealOropen());

            File file = new File(picture.getFile());
            fileHashMap.put("file", file);

            String result = ConnectionUtil.getResponse(Constants.HOST + Constants.UPLOAD_PICTURE, hashMap, fileHashMap);
            returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
            if (returnObject != null) {
                picture.setHavepost(true);
                mainApp.getDaoSession().getPictureDao().update(picture);
            }
        }
    }
}
