package com.huiwu.temperaturecontrol.bean;

import android.os.Environment;

/**
 * Created by HuiWu on 2016/4/11.
 */
public class Constants {
    public static String getStoragePath() {
        String path = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";
        if (path.endsWith("/")) {
            return path + "Pictures/ll";
        }
        return path + "/Pictures/ll";
    }

    public static final String SHARED = "SHARED";

    public static final String DB_NAME = "temperature-db";

    public static final String USER_INFO = "USER_INFO";

    public static final String LOGIN_TIME = "LOGIN_TIME";

    public static final String USER_IMAGE = "USER_IMAGE";

    public static final String LOGIN_AGAIN = "LOGIN_AGAIN";

    public static final String APP_NAME = "llRFID";

    public static final String VERSION = "version3.00";

    public static final String IS_ON_LINE = "IS_ON_LINE";

    public static final String TAG_INFO = "TAG_INFO";

    public static final String TIME_ARRAY = "TIME_ARRAY";

    public static final String SELECT_OBJECT = "SELECT_OBJECT";

    public static final String READ_UID = "READ_UID";

    public static final String LOCK = "LOCK";

    public static final String PICTURE_FILE = "PICTURE_FILE";

    public static String HOST = "http://hello.yunrfid.com";

    public static final String LOGIN_URL = HOST + "/CoreSYS.SYS/LGKeyLogin.ajax";

    public static final String REVISE_URL = HOST + "/CoreSYS.SYS/ChangePwd.ajax";

    public static final String MANAGE_URL = HOST + "/TemperatureTag.BaseTemperatureTag";

    public static final String GET_DATA_URL = MANAGE_URL + "/GetDataOffLine.ajax";

    public static final String BIND_TAG_OFF_LINE_URL = MANAGE_URL + "/BoxBindOffLine.ajax";

    public static final String UNBIND_TAG_URL = MANAGE_URL + "/RemoveBoxBind.ajax";

    public static final String UPLOAD_DATA_URL = MANAGE_URL + "/GatherTemperatures.ajax";

    public static final String UPLOAD_DATA_OFFLINE_URL = MANAGE_URL + "/GatherTemperaturesOffLine.ajax";

    public static final String GET_TEMPERATURE_LINKS_URL = MANAGE_URL + "/GetTemperatureLinks_RawJson.ajax";

    public static final String GET_GATHER_TEMPERATURE_RECORDS_URL = MANAGE_URL + "/GetBoxLinkTmpCntRecord.ajax";

    public static final String GET_GATHER_TEMPERATURE_DATA_URL = MANAGE_URL + "/GetGatherTemperatures.ajax";

    public static final String CHECK_BOX_STATUS = MANAGE_URL + "/checkBox.ajax";

    public static final String ADD_BOX = MANAGE_URL + "/AddBox.ajax";

    public static final String GET_PARENT_GOODS_URL = MANAGE_URL + "/GetGoodType.ajax";

    public static final String GET_CHILD_GOODS_URL = MANAGE_URL + "/ListCGoodType_RawJson.ajax";

    public static final String GET_ALL_OBJECTS_URL = MANAGE_URL + "/ListRfidGoods_RawJson.ajax";

    public static final String SEAL_TAG = MANAGE_URL + "/tmpSeal.ajax";

    public static final String OPEN_TAG = MANAGE_URL + "/tmpOpen.ajax";

    public static final String UPLOAD_PICTURE = MANAGE_URL + "/uploadFileI.ajax";

    public static final String CHECK_NEW_LOCK = MANAGE_URL + "/checkNewLock.ajax";

}
