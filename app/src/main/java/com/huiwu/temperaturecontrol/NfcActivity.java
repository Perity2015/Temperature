package com.huiwu.temperaturecontrol;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.aofei.nfc.TagUtil;
import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;
import com.huiwu.temperaturecontrol.nfc.Helper;
import com.huiwu.temperaturecontrol.nfc.NFCCommand;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class NfcActivity extends NfcBaseActivity {
    public static final int NFC_GATHER = 0;
    public static final int NFC_CONFIG = 1;
    public static final int NFC_READ_UID = 2;
    public static final int NFC_UNBIND = 3;
    public static final int NFC_OPEN = 4;
    public static final int NFC_PASSWORD = 5;

    public static final String COMMAND_PARAM = "command";
    private int command;

    private JSONModel.TagInfo tagInfo;

    private int times = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_back);

        ImageView image_tag = (ImageView) findViewById(R.id.image_tag);
        image_tag.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.tag_right_to_left));

        command = getIntent().getIntExtra(COMMAND_PARAM, NFC_GATHER);
        TextView textView = (TextView) findViewById(R.id.text_nfc_title);
        String[] nfcCommands = getResources().getStringArray(R.array.nfcCommands);
        textView.setText(nfcCommands[command]);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            mainApp.setCurrentTag(tagFromIntent);
            mainApp.setUid(bytesToHexString(tagFromIntent.getId()));

            switch (command) {
                case NFC_GATHER:
                    new ReadTagInfoTask().execute(tagFromIntent);
                    break;
                case NFC_CONFIG:
                    new WriteConfigDataTask().execute(tagFromIntent);
                    break;
                case NFC_READ_UID:
                    setResult(RESULT_OK, getIntent().putExtra(Constants.read_uid, bytesToHexString(tagFromIntent.getId())));
                    finish();
                    break;
                case NFC_UNBIND:
                    unBindTag();
                    break;
                case NFC_OPEN:
                case NFC_PASSWORD:
                    JSONModel.Lock lock = getIntent().getParcelableExtra(Constants.lock);

                    boolean flag = false;
                    if (times < 3) {
                        times += 1;
                        if (lock.isNewPwd()) {
                            flag = openLock(intent, lock.getFirstpwd(), lock.getLockpwd());
                        } else {
                            flag = openLock(intent, lock.getLockpwd(), lock.getLockpwd());
                        }
                    } else if (times < 6) {
                        times += 1;
                        if (lock.isNewPwd()) {
                            flag = openLock(intent, lock.getLockpwd(), lock.getLockpwd());
                        } else {
                            flag = openLock(intent, lock.getFirstpwd(), lock.getLockpwd());
                        }
                    }
                    if (times == 6) {
                        times = 0;
                    }
                    if (flag) {
                        showOpenDialog();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOpenDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle(R.string.notice);
        builder.setMessage("确认是否已开锁？");
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private class ReadTagInfoTask extends AsyncTask<Tag, Void, Boolean> {
        byte[] addressStart;
        byte[] numberOfBlockToRead;
        byte[] ReadMultipleBlockAnswer;
        String sNbOfBlock;
        int nBlocks;
        int index;
        String message = getString(R.string.read_tag_lost);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.keep_tag_close_to_nfc));
            progressDialog.show();
            tagInfo = new JSONModel.TagInfo();
        }

        @Override
        protected Boolean doInBackground(Tag... params) {
            try {
                byte[] GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(mainApp.getCurrentTag(), mainApp);
                if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
                    String startAddressString = "0000";
                    startAddressString = Helper.castHexKeyboard(startAddressString);
                    startAddressString = Helper.FormatStringAddressStart(startAddressString, mainApp);
                    addressStart = Helper.ConvertStringToHexBytes(startAddressString);
                    sNbOfBlock = Helper.FormatStringNbBlockInteger("0015", startAddressString, mainApp);
                    numberOfBlockToRead = Helper.ConvertIntTo2bytesHexaFormat(Integer.parseInt(sNbOfBlock));
                }
                ReadMultipleBlockAnswer = null;
                readData();
                nBlocks = Integer.parseInt(sNbOfBlock);
                if (ReadMultipleBlockAnswer != null && ReadMultipleBlockAnswer.length - 1 > 0) {
                    if (ReadMultipleBlockAnswer[0] == 0x00) {
                        if (!gatherConfigData()) {
                            return false;
                        }
                        String startAddressString = "0000";
                        startAddressString = Helper.castHexKeyboard(startAddressString);
                        startAddressString = Helper.FormatStringAddressStart(startAddressString, mainApp);
                        addressStart = Helper.ConvertStringToHexBytes(startAddressString);
                        if (tagInfo.getRoundCircle() == 0) {
                            if (tagInfo.isJustTemp()) {
                                sNbOfBlock = Helper.FormatStringNbBlockInteger(String.format("%04d", (index + 1) / 2 + 48), startAddressString, mainApp);
                            } else {
                                sNbOfBlock = Helper.FormatStringNbBlockInteger(String.format("%04d", index + 48), startAddressString, mainApp);
                            }
                        } else {
                            sNbOfBlock = Helper.FormatStringNbBlockInteger(String.format("%04d", 2048), startAddressString, mainApp);
                        }
                        numberOfBlockToRead = Helper.ConvertIntTo2bytesHexaFormat(Integer.parseInt(sNbOfBlock));
                        ReadMultipleBlockAnswer = null;
                        readData();
                        nBlocks = Integer.parseInt(sNbOfBlock);
                        if (ReadMultipleBlockAnswer != null && ReadMultipleBlockAnswer.length - 1 > 0) {
                            if (ReadMultipleBlockAnswer[0] == 0x00) {
                                gatherTempAndHumData();
                                sqLiteManage.insertRecords(tagInfo);
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void readData() {
            int cpt = 0;
            if (mainApp.isMultipleReadSupported() == false || Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <= 1) {
                while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
                    ReadMultipleBlockAnswer = NFCCommand.Send_several_ReadSingleBlockCommands_NbBlocks(mainApp.getCurrentTag(), addressStart, numberOfBlockToRead, mainApp);
                    cpt++;
                }
            } else if (Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
                while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
                    ReadMultipleBlockAnswer = NFCCommand.SendReadMultipleBlockCommandCustom(mainApp.getCurrentTag(), addressStart, numberOfBlockToRead[1], mainApp);
                    cpt++;
                }
            } else {
                while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
                    ReadMultipleBlockAnswer = NFCCommand.SendReadMultipleBlockCommandCustom2(mainApp.getCurrentTag(), addressStart, numberOfBlockToRead, mainApp);
                    cpt++;
                }
            }
        }

        /**
         * @return
         */
        private boolean gatherConfigData() {
            String linkuuid = "";
            for (int i = 29; i <= 44; i++) {
                linkuuid += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[i]);
            }
            tagInfo = sqLiteManage.getLastTagInfo(linkuuid, mainApp.getUid().toUpperCase());
            tagInfo.setLinkuuid(linkuuid.toLowerCase());

            if (ReadMultipleBlockAnswer[12] == 0x33
                    || ReadMultipleBlockAnswer[12] == 0x53) {
                tagInfo.setJustTemp(false);
            } else if (ReadMultipleBlockAnswer[12] == 0x31
                    || ReadMultipleBlockAnswer[12] == 0x51) {
                tagInfo.setJustTemp(true);
            } else {
                message = getString(R.string.tag_type_error);
                return false;
            }

            //此处存储的为‘慧物’GBK格式byte[]
            if (ReadMultipleBlockAnswer[25] != -69
                    || ReadMultipleBlockAnswer[26] != -37
                    || ReadMultipleBlockAnswer[27] != -50
                    || ReadMultipleBlockAnswer[28] != -17) {
                message = getString(R.string.now_power_tag);
                return false;
            }

            String companyIdStr = Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[57]);
            companyIdStr += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[58]);
            companyIdStr += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[59]);
            companyIdStr += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[60]);
            long companyId = Long.parseLong(companyIdStr, 16);
            if (companyId != userInfo.getUserPower().getCompanyid()) {
                message = getString(R.string.tag_not_belong_company);
                return false;
            }
            index = ReadMultipleBlockAnswer[18] * 256 + (ReadMultipleBlockAnswer[17] >= 0 ? ReadMultipleBlockAnswer[17] : 256 + ReadMultipleBlockAnswer[17]);
            if (!tagInfo.isJustTemp()) {
                index = index / 2;
            }
            tagInfo.setIndex(index);
            if (index == 0) {
                message = getString(R.string.tag_no_data);
                return false;
            }
            if (ReadMultipleBlockAnswer[1] == 17) {
                message = getString(R.string.tag_was_stop);
                return false;
            }
            tagInfo.setUid(mainApp.getUid().toUpperCase());
            tagInfo.setRecordStatus(ReadMultipleBlockAnswer[11]);

            int onetime = ReadMultipleBlockAnswer[2];
            JSONModel.Goods goods;
            if (tagInfo.getGoods() == null) {
                goods = new JSONModel.Goods();
            } else {
                goods = tagInfo.getGoods();
            }
            goods.setOnetime(onetime);
            goods.setLowtmpnumber(ReadMultipleBlockAnswer[13]);
            goods.setHightmpnumber(ReadMultipleBlockAnswer[14]);
            goods.setLowhumiditynumber(ReadMultipleBlockAnswer[15]);
            goods.setHighhumiditynumber(ReadMultipleBlockAnswer[16]);
            tagInfo.setGoods(goods);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0);
            tagInfo.setReadTime(calendar.getTimeInMillis());

            tagInfo.setRoundCircle(ReadMultipleBlockAnswer[19]);
            tagInfo.setPower(ReadMultipleBlockAnswer[20]);

            String startTimeStr = Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[45]);
            startTimeStr += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[46]);
            startTimeStr += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[47]);
            startTimeStr += Helper.ConvertHexByteToString(ReadMultipleBlockAnswer[48]);
            long startTime = Long.parseLong(startTimeStr, 16) * 1000L;
            calendar.setTimeInMillis(startTime);
            calendar.set(Calendar.YEAR, 2000 + Integer.parseInt(String.format("%02x", ReadMultipleBlockAnswer[10])));
            calendar.set(Calendar.MONTH, Integer.parseInt(String.format("%02x", ReadMultipleBlockAnswer[9])) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(String.format("%02x", ReadMultipleBlockAnswer[8])));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(String.format("%02x", ReadMultipleBlockAnswer[7])));
            calendar.set(Calendar.MINUTE, Integer.parseInt(String.format("%02x", ReadMultipleBlockAnswer[6])));
//                calendar.set(Calendar.SECOND, Integer.parseInt(String.format("%02x", ReadMultipleBlockAnswer[5])));
            long endTime = calendar.getTimeInMillis();

            tagInfo.setEndTime(endTime);
            if (tagInfo.getRoundCircle() > 0) {
                if (tagInfo.isJustTemp()) {
                    tagInfo.setStartTime(endTime - 3999 * onetime * 60 * 1000L);
                } else {
                    tagInfo.setStartTime(endTime - 1999 * onetime * 60 * 1000L);
                }
            } else {
                tagInfo.setStartTime(endTime - (index - 1) * onetime * 60 * 1000L);
            }
            return true;
        }

        private void gatherTempAndHumData() {
            if (tagInfo.getRoundCircle() > 0) {
                if (tagInfo.isJustTemp()) {
                    for (int i = index; i < 4000; i++) {
                        double temp = Convert2bytesHexFormatToInt(new byte[]{ReadMultipleBlockAnswer[i * 2 + 2 + 48 * 4], ReadMultipleBlockAnswer[i * 2 + 1 + 48 * 4]}) / 100.00D;
                        double hum = temp;
                        if (i == index) {
                            tagInfo.setTemp_min(temp);
                            tagInfo.setTemp_max(temp);
                            tagInfo.setHum_max(hum);
                            tagInfo.setHum_min(hum);
                        }
                        checkIsOutLimit(temp, hum);
                    }
                } else {
                    for (int i = index; i < 2000; i++) {
                        double temp = Convert2bytesHexFormatToInt(new byte[]{ReadMultipleBlockAnswer[i * 4 + 2 + 48 * 4], ReadMultipleBlockAnswer[i * 4 + 1 + 48 * 4]}) / 100.00D;
                        double hum = Convert2bytesHexFormatToInt(new byte[]{ReadMultipleBlockAnswer[i * 4 + 4 + 48 * 4], ReadMultipleBlockAnswer[i * 4 + 3 + 48 * 4]}) / 100.00D;
                        if (i == index) {
                            tagInfo.setTemp_min(temp);
                            tagInfo.setTemp_max(temp);
                            tagInfo.setHum_max(hum);
                            tagInfo.setHum_min(hum);
                        }
                        checkIsOutLimit(temp, hum);
                    }
                }
            }
            if (tagInfo.isJustTemp()) {
                for (int i = 0; i < index; i++) {
                    double temp = Convert2bytesHexFormatToInt(new byte[]{ReadMultipleBlockAnswer[i * 2 + 2 + 48 * 4], ReadMultipleBlockAnswer[i * 2 + 1 + 48 * 4]}) / 100.00D;
                    double hum = temp;
                    if (i == 0 && tagInfo.getRoundCircle() == 0) {
                        tagInfo.setTemp_min(temp);
                        tagInfo.setTemp_max(temp);
                        tagInfo.setHum_max(hum);
                        tagInfo.setHum_min(hum);
                    }
                    if (i == index - 1) {
                        tagInfo.setTem_now(temp);
                        tagInfo.setHum_now(hum);
                    }
                    checkIsOutLimit(temp, hum);
                }
            } else {
                for (int i = 0; i < index; i++) {
                    double temp = Convert2bytesHexFormatToInt(new byte[]{ReadMultipleBlockAnswer[i * 4 + 2 + 48 * 4], ReadMultipleBlockAnswer[i * 4 + 1 + 48 * 4]}) / 100.00D;
                    double hum = Convert2bytesHexFormatToInt(new byte[]{ReadMultipleBlockAnswer[i * 4 + 4 + 48 * 4], ReadMultipleBlockAnswer[i * 4 + 3 + 48 * 4]}) / 100.00D;
                    if (i == 0 && tagInfo.getRoundCircle() == 0) {
                        tagInfo.setTemp_min(temp);
                        tagInfo.setTemp_max(temp);
                        tagInfo.setHum_max(hum);
                        tagInfo.setHum_min(hum);
                    }
                    if (i == index - 1) {
                        tagInfo.setTem_now(temp);
                        tagInfo.setHum_now(hum);
                    }
                    checkIsOutLimit(temp, hum);
                }
            }
        }

        private void checkIsOutLimit(double temp, double hum) {
            tagInfo.getTempList().add(temp);
            tagInfo.getHumList().add(hum);
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

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if (!aBoolean) {
                Utils.showLongToast(message, mContext);
                return;
            }
            Intent intent = new Intent(mContext, ChartActivity.class);
            intent.putExtra(Constants.tag_info, tagInfo);
            startActivity(intent);
        }
    }

    private class WriteConfigDataTask extends AsyncTask<Tag, Void, Boolean> {
        byte[] writeConfigBytes;
        byte[] writeInfoBytes;
        String message = getString(R.string.read_tag_lost);
        String linkuuid;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tagInfo = getIntent().getParcelableExtra(Constants.tag_info);
            linkuuid = UUID.randomUUID().toString().replace("-", "");

            tagInfo.setUid(mainApp.getUid().toUpperCase());
            tagInfo.setLinkuuid(linkuuid);
            JSONModel.Box box = tagInfo.getBox();
            JSONModel.Goods goods = tagInfo.getGoods();

            String writeConfigStr = "11";
            writeConfigStr += Helper.ConvertHexByteToString((byte) goods.getOnetime());
            writeConfigStr += Helper.ConvertHexByteToString((byte) tagInfo.getDelayTime());
            writeConfigStr += "10";
            Calendar calendar = Calendar.getInstance();
            writeConfigStr += DateFormat.format("ssmmkkddMMyy", calendar);
            writeConfigStr += "0033";
            writeConfigStr += Helper.ConvertHexByteToString((byte) goods.getLowtmpnumber());
            writeConfigStr += Helper.ConvertHexByteToString((byte) goods.getHightmpnumber());
            writeConfigStr += Helper.ConvertHexByteToString((byte) goods.getLowhumiditynumber());
            writeConfigStr += Helper.ConvertHexByteToString((byte) goods.getHighhumiditynumber());
            writeConfigStr += "00000004";

            writeConfigBytes = new byte[writeConfigStr.length() / 2];

            for (int i = 0; i < writeConfigBytes.length; i++) {
                writeConfigBytes[i] = Helper.ConvertStringToHexByte(writeConfigStr.substring(i * 2, i * 2 + 2));
            }

            String writeInfoStr = "BBDBCEEF";
            writeInfoStr += linkuuid;
            writeInfoStr += String.format("%08x", calendar.getTimeInMillis() / 1000L + tagInfo.getDelayTime() * 60);
            writeInfoStr += String.format("%08x", box.getBoxid());
            writeInfoStr += String.format("%08x", goods.getId());
            writeInfoStr += String.format("%08x", userInfo.getUserPower().getCompanyid());
            writeInfoStr += "00000000";

            writeInfoBytes = new byte[writeInfoStr.length() / 2];

            for (int i = 0; i < writeInfoBytes.length; i++) {
                writeInfoBytes[i] = Helper.ConvertStringToHexByte(writeInfoStr.substring(i * 2, i * 2 + 2));
            }
        }

        @Override
        protected Boolean doInBackground(Tag... params) {
            byte[] GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(mainApp.getCurrentTag(), mainApp);
            if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
                byte[] ReadSingleBlockAnswer = NFCCommand.Send_several_ReadSingleBlockCommands_NbBlocks(mainApp.getCurrentTag(), new byte[]{0x00, 0x02}, new byte[]{0x00, 0x01}, mainApp);
                if (ReadSingleBlockAnswer != null && ReadSingleBlockAnswer[0] == 0) {
//					if (ReadSingleBlockAnswer[4] == 0x31 || ReadSingleBlockAnswer[4] == 0x51){
//						message = getString(R.string.tag_type_single_temp);
//					}
                    if (ReadSingleBlockAnswer[4] != 0x33
                            && ReadSingleBlockAnswer[4] != 0x53
                            && ReadSingleBlockAnswer[4] != 0x31
                            && ReadSingleBlockAnswer[4] != 0x51) {
                        message = getString(R.string.tag_type_error);
                        return false;
                    }
                    writeConfigBytes[11] = ReadSingleBlockAnswer[4];
                } else {
                    return false;
                }
                ReadSingleBlockAnswer = NFCCommand.Send_several_ReadSingleBlockCommands_NbBlocks(mainApp.getCurrentTag(), new byte[]{0x00, 0x04}, new byte[]{0x00, 0x01}, mainApp);
                if (ReadSingleBlockAnswer != null && ReadSingleBlockAnswer[0] == 0) {
                    writeConfigBytes[19] = ReadSingleBlockAnswer[4];
                }
                byte[] PasswordData = new byte[]{-107, 0, 0, 0};
                byte[] passwordResponse = NFCCommand.SendPresentPasswordCommand(params[0], (byte) 0x01, PasswordData, mainApp);
                if (!(passwordResponse.length == 1 && passwordResponse[0] == 0)) {
                    PasswordData = new byte[]{-74, 0, 0, 0};
                    passwordResponse = NFCCommand.SendPresentPasswordCommand(params[0], (byte) 0x01, PasswordData, mainApp);
                    if (!(passwordResponse.length == 1 && passwordResponse[0] == 0)) {
                        PasswordData = new byte[]{0, 0, 0, 0};
                        passwordResponse = NFCCommand.SendPresentPasswordCommand(params[0], (byte) 0x01, PasswordData, mainApp);
                        if (!(passwordResponse.length == 1 && passwordResponse[0] == 0)) {
                            message = getString(R.string.tag_was_write_code);
                            return false;
                        }
                    }
                }
                byte[] response = NFCCommand.SendWriteMultipleBlockCommand(params[0], new byte[]{0x00, 0x00}, writeConfigBytes, mainApp);
                if (response != null && response[0] == 0x00) {
                    response = NFCCommand.SendWriteMultipleBlockCommand(params[0], new byte[]{0x00, 0x06}, writeInfoBytes, mainApp);
                    if (response != null && response[0] == 0x00) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (!aBoolean) {
                Utils.showLongToast(message, mContext);
                return;
            }
            bindTag();
        }
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
        map.put("rfid", mainApp.getUid().toUpperCase());
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
                Utils.showLongToast(result, mContext);
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    if (returnObject.getM_ReturnOBJJsonObject().has("isUse") && returnObject.getM_ReturnOBJJsonObject().get("isUse").getAsBoolean()) {
                        showUnbindDialog(returnObject.getsMsg());
                    }
                    Utils.showLongToast(returnObject.getsMsg(), mContext);
                    return;
                }
                sqLiteManage.insertFirstTagInfo(tagInfo);
                setResult(RESULT_OK);
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
        map.put("rfid", mainApp.getUid().toUpperCase());
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
                    if (command == NFC_UNBIND) {
                        finish();
                    }
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

    private boolean openLock(Intent intent, String old_key, String new_key) throws Exception {
        boolean flag;
        TagUtil mTagUtil = TagUtil.selectTag(intent, false);


        byte[] keyBytes = string2Bytes(old_key, 16);
        String keyStr = bytes2HexString(keyBytes, 0, keyBytes.length);


        flag = mTagUtil.authentication(intent, keyStr, false);


        flag = writeNewKey(mTagUtil, intent, new_key);

        TLog.d("TAG", flag + "");
        mTagUtil.close();
        return flag;
    }

    /**
     * Write New Password
     *
     * @param mTagUtil
     * @param intent
     * @param newKey
     * @return
     */
    private boolean writeNewKey(TagUtil mTagUtil, Intent intent, String newKey) {
        boolean b;
        byte[] keyBytes = string2Bytes(newKey, 16);
        byte[] writeBytes = new byte[4];
        writeBytes[0] = keyBytes[7];
        writeBytes[1] = keyBytes[6];
        writeBytes[2] = keyBytes[5];
        writeBytes[3] = keyBytes[4];
        b = mTagUtil.writeTag(intent, (byte) 245, writeBytes, false);
        if (!b) {
            return false;
        }
        writeBytes[0] = keyBytes[3];
        writeBytes[1] = keyBytes[2];
        writeBytes[2] = keyBytes[1];
        writeBytes[3] = keyBytes[0];
        b = mTagUtil.writeTag(intent, (byte) 246, writeBytes, false);
        if (!b) {
            return false;
        }
        writeBytes[0] = keyBytes[15];
        writeBytes[1] = keyBytes[14];
        writeBytes[2] = keyBytes[13];
        writeBytes[3] = keyBytes[12];
        b = mTagUtil.writeTag(intent, (byte) 247, writeBytes, false);
        if (!b) {
            return false;
        }
        writeBytes[0] = keyBytes[11];
        writeBytes[1] = keyBytes[10];
        writeBytes[2] = keyBytes[9];
        writeBytes[3] = keyBytes[8];
        b = mTagUtil.writeTag(intent, (byte) 248, writeBytes, false);
        return b;
    }

    public static String bytes2HexString(byte[] bytes, int start, int end) {
        StringBuffer sb = new StringBuffer();

        for (int i = start; i < start + end; ++i) {
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(bytes[i])}));
        }

        return sb.toString();
    }

    public static byte[] string2Bytes(String s, int length) {
        byte[] bytes1 = s.getBytes();
        byte[] bytes2 = new byte[length];
        System.arraycopy(bytes1, 0, bytes2, 0, Math.min(length, bytes1.length));
        return bytes2;
    }

}
