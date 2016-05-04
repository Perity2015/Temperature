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

    public static String shared = "shared";

    public static String user_info = "user_info";

    public static String login_time = "login_time";

    public static String user_image = "user_image";

    public static String login_again = "login_again";

    public static String app_name = "llRFID";

    public static String version = "version3.00";

    public static String position = "position";

    public static String is_off_line = "is_off_line";

    public static String is_on_line = "is_on_line";

    public static String tag_info = "tagInfo";

    public static String select_object = "select_object";

    public static String read_uid = "read_uid";

    public static String lock = "lock";

    public static String picture_file = "picture_file";

    public static String host = "http://10.0.0.200:88";

    public static String login_url = host + "/CoreSYS.SYS/LGKeyLogin.ajax";

    public static String revise_url = host + "/CoreSYS.SYS/ChangePwd.ajax";

    public static String manage_url = host + "/TemperatureTag.BaseTemperatureTag";

    public static String get_data_url = manage_url + "/GetDataOffLine.ajax";

    public static String bind_tag_offLine_url = manage_url + "/BoxBindOffLine.ajax";

    public static String unbind_tag_url = manage_url + "/RemoveBoxBind.ajax";

    public static String upload_data_url = manage_url + "/GatherTemperatures.ajax";

    public static String upload_data_offline_url = manage_url + "/GatherTemperaturesOffLine.ajax";

    public static String get_temperature_links_url = manage_url + "/GetTemperatureLinks_RawJson.ajax";

    public static String get_gather_temperature_records_url = manage_url + "/GetBoxLinkTmpCntRecord.ajax";

    public static String get_gather_temperature_data_url = manage_url + "/GetGatherTemperatures.ajax";

    public static String check_box_status = manage_url + "/checkBox.ajax";

    public static String add_box = manage_url + "/AddBox.ajax";

    public static String get_parent_goods_url = manage_url + "/GetGoodType.ajax";

    public static String get_child_goods_url = manage_url + "/ListCGoodType_RawJson.ajax";

    public static String get_all_objects_url = manage_url + "/ListRfidGoods_RawJson.ajax";

    public static String seal_tag = manage_url + "/tmpSeal.ajax";

    public static String open_tag = manage_url + "/tmpOpen.ajax";

    public static String upload_picture = manage_url + "/uploadFileI.ajax";

    public static String check_new_lock = manage_url + "/checkNewLock.ajax";

}
