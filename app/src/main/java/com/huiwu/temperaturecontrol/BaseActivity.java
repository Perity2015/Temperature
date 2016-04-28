package com.huiwu.temperaturecontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.temperaturecontrol.application.MainApp;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.nfc.Helper;
import com.huiwu.temperaturecontrol.sqlite.SQLiteManage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by HuiWu on 2016/4/11.
 */
public class BaseActivity extends AppCompatActivity {
    public Context mContext;
    public SharedPreferences mShared;
    public Gson gson = new Gson();
    public JSONModel.UserInfo userInfo;
    public SQLiteManage sqLiteManage;

    public ProgressDialog progressDialog;
    public ConnectionTask task;
    public MainApp mainApp;

    public static final int REQUEST_LOGIN_AGAIN = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mainApp = (MainApp) getApplication();
        mShared = getSharedPreferences(Constants.shared, MODE_PRIVATE);
        sqLiteManage = SQLiteManage.getInstance(mContext);
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    cancelConnectionTask();
                }
                return false;
            }
        });
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelConnectionTask();
            }
        });

        try {
            userInfo = gson.fromJson(mShared.getString(Constants.user_info, ""), JSONModel.UserInfo.class);
        } catch (Exception e) {
            userInfo = null;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mainApp.startLocation();
    }

    @Override
    protected void onResume() {
        try {
            userInfo = gson.fromJson(mShared.getString(Constants.user_info, ""), JSONModel.UserInfo.class);
        } catch (Exception e) {

        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void cancelConnectionTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
    }

    public HashMap<String, String> getDefaultMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("LGKey", userInfo.getLGKey());
        return map;
    }

    public void loginAgain() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Constants.login_again, true);
        startActivityForResult(intent, REQUEST_LOGIN_AGAIN);
    }

    public void showNoticeDialog(String message, final boolean needFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setCancelable(!needFinish);
        builder.setMessage(message);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (needFinish) {
                    finish();
                }
            }
        });
        builder.show();
    }

    public void hideSoftInput() {
        if (getCurrentFocus() != null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < bArray.length; i++) {
            sb.append(Helper.ConvertHexByteToString(bArray[bArray.length - 1 - i]));
        }

        return sb.toString();
    }

    public int Convert2bytesHexFormatToInt(byte[] bytes) {
        if (bytes[0] >= 0) {
            if (bytes[1] < 0) {
                return 256 + bytes[1] + 256 * bytes[0];
            }
            return bytes[1] + 256 * bytes[0];
        } else {
            if (bytes[1] < 0) {
                return 256 + bytes[1] + 256 + 256 * bytes[0];
            }
            return bytes[1] + 256 + 256 * bytes[0];
        }
    }

    public DisplayImageOptions getDefaultDisplayImageOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.image_default) // 设置图片在下载期间显示的图片
//                .showImageForEmptyUri(R.drawable.image_default)// 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.mipmap.ic_launcher)  // 设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)// 设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  // 是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY)// 设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                //.delayBeforeLoading(int delayInMillis)//int delayInMillis为你设置的下载前的延迟时间
                //设置图片加入缓存前，对bitmap进行设置
                //.preProcessor(BitmapProcessor preProcessor)
                .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
                //.displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
                .displayer(new FadeInBitmapDisplayer(1000))// 是否图片加载好后渐入的动画时间
                .build();
        return options;
    }

    public void dealWithPicture(final ImageView image_picture, final ImageView image_delete, final File file) {
        ImageLoader.getInstance().loadImage("file:///" + file.getAbsolutePath(), getDefaultDisplayImageOptions(), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                progressDialog.setMessage(getString(R.string.deal_with_picture));
                progressDialog.show();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                image_delete.setVisibility(View.VISIBLE);
                image_picture.setTag(file.getAbsolutePath());
                image_picture.setImageBitmap(loadedImage);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                progressDialog.dismiss();
            }
        });
    }

    public void showLocalPicture(ImageView imageView) {
        ImageLoader.getInstance().displayImage("file:///" + imageView.getTag(), imageView, getDefaultDisplayImageOptions());
    }

    public boolean DecodeGetSystemInfoResponse(byte[] GetSystemInfoResponse) {
        //if the tag has returned a good response
        if (GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12) {
            String uidToString = "";
            byte[] uid = new byte[8];
            // change uid format from byteArray to a String
            for (int i = 1; i <= 8; i++) {
                uid[i - 1] = GetSystemInfoResponse[10 - i];
                uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
            }

            //***** TECHNO ******
            mainApp.setUid(uidToString);
            if (uid[0] == (byte) 0xE0)
                mainApp.setTechno("ISO 15693");
            else if (uid[0] == (byte) 0xD0)
                mainApp.setTechno("ISO 14443");
            else
                mainApp.setTechno("Unknown techno");

            //***** MANUFACTURER ****
            if (uid[1] == (byte) 0x02)
                mainApp.setManufacturer("STMicroelectronics");
            else if (uid[1] == (byte) 0x04)
                mainApp.setManufacturer("NXP");
            else if (uid[1] == (byte) 0x07)
                mainApp.setManufacturer("Texas Instrument");
            else
                mainApp.setManufacturer("Unknown manufacturer");

            //**** PRODUCT NAME *****
            if (uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07) {
                mainApp.setProductName("LRI512");
                mainApp.setMultipleReadSupported(false);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17) {
                mainApp.setProductName("LRI64");
                mainApp.setMultipleReadSupported(false);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23) {
                mainApp.setProductName("LRI2K");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B) {
                mainApp.setProductName("LRIS2K");
                mainApp.setMultipleReadSupported(false);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F) {
                mainApp.setProductName("M24LR64");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43) {
                mainApp.setProductName("LRI1K");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47) {
                mainApp.setProductName("LRIS64K");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B) {
                mainApp.setProductName("M24LR01E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F) {
                mainApp.setProductName("M24LR16E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
                if (mainApp.isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53) {
                mainApp.setProductName("M24LR02E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(false);
            } else if (uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57) {
                mainApp.setProductName("M24LR32E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
                if (mainApp.isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B) {
                mainApp.setProductName("M24LR04E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F) {
                mainApp.setProductName("M24LR64E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
                if (mainApp.isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63) {
                mainApp.setProductName("M24LR08E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
            } else if (uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67) {
                mainApp.setProductName("M24LR128E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
                if (mainApp.isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F) {
                mainApp.setProductName("M24LR256E");
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
                if (mainApp.isBasedOnTwoBytesAddress() == false)
                    return false;
            } else if (uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB) {
                mainApp.setProductName("detected product");
                mainApp.setBasedOnTwoBytesAddress(true);
                mainApp.setMultipleReadSupported(true);
                mainApp.setMemoryExceed2048bytesSize(true);
            } else {
                mainApp.setProductName("Unknown product");
                mainApp.setBasedOnTwoBytesAddress(false);
                mainApp.setMultipleReadSupported(false);
                mainApp.setMemoryExceed2048bytesSize(false);
            }

            //*** DSFID ***
            mainApp.setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));

            //*** AFI ***
            mainApp.setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));

            //*** MEMORY SIZE ***
            if (mainApp.isBasedOnTwoBytesAddress()) {
                String temp = new String();
                temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
                temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
                mainApp.setMemorySize(temp);
            } else
                mainApp.setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));

            //*** BLOCK SIZE ***
            if (mainApp.isBasedOnTwoBytesAddress())
                mainApp.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
            else
                mainApp.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

            //*** IC REFERENCE ***
            if (mainApp.isBasedOnTwoBytesAddress())
                mainApp.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
            else
                mainApp.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));

            return true;
        }

        //if the tag has returned an error code
        else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}


