package com.huiwu.temperaturecontrol;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.utils.Utils;
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

    public NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    public ProgressDialog progressDialog;
    public MainApp mainApp;

    public static final int REQUEST_LOGIN_AGAIN = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mainApp = (MainApp) getApplication();
        mShared = getSharedPreferences(Constants.SHARED, MODE_PRIVATE);
        sqLiteManage = SQLiteManage.getInstance(mContext);
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    ConnectionUtil.cancelHttp();
                }
                return false;
            }
        });
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ConnectionUtil.cancelHttp();
            }
        });

        String user = mShared.getString(Constants.USER_INFO, "{}");
        try {
            userInfo = gson.fromJson(new String(Base64.decode(user.getBytes(), Base64.DEFAULT)), JSONModel.UserInfo.class);
        } catch (Exception e) {
            userInfo = null;
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mFilters = new IntentFilter[]{ndef};
        mTechLists = new String[][]{new String[]{android.nfc.tech.NfcV.class.getName()}, new String[]{android.nfc.tech.NfcA.class.getName()}};
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainApp.startLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            if (mNfcAdapter.isEnabled()) {
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
            } else {
                Utils.showLongToast(getString(R.string.please_open_nfc), mContext);
                openNfc();
            }
        }
        String user = mShared.getString(Constants.USER_INFO, "{}");
        try {
            userInfo = gson.fromJson(new String(Base64.decode(user.getBytes(), Base64.DEFAULT)), JSONModel.UserInfo.class);
        } catch (Exception e) {
            userInfo = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void openNfc() {
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    public HashMap<String, String> getDefaultMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("LGKey", userInfo.getM_UserInfo().getLGKey());
        return map;
    }

    public void loginAgain() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Constants.LOGIN_AGAIN, true);
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
            sb.append(Helper.ConvertHexByteToString(bArray[i]));
        }

        return sb.toString();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}


