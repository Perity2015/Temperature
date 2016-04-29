package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.ShowPictureActivity;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.service.SyncService;

import java.io.File;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SealFragment extends Fragment {


    @Bind(R.id.text_address)
    TextView textAddress;
    @Bind(R.id.text_box_no)
    TextView textBoxNo;
    @Bind(R.id.text_seal_title)
    TextView textSealTitle;
    @Bind(R.id.text_seal)
    TextView textSeal;
    @Bind(R.id.image_picture)
    ImageView imagePicture;
    @Bind(R.id.image_delete)
    ImageView imageDelete;

    private final int REQUEST_READ_UID = 101;
    private final int REQUEST_PHOTO_CAMERA = 102;
    private final int REQUEST_PICTURE_PREVIEW = 103;
    private final int REQUEST_PASSWORD = 104;
    @Bind(R.id.text_goods)
    TextView textGoods;
    @Bind(R.id.text_object)
    TextView textObject;

    private ManageActivity manageActivity;

    private String picName;

    private String rfid;

    public SealFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        manageActivity = (ManageActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seal, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textAddress.requestFocus();
        textGoods.setText(manageActivity.tempLink.getGoodtype() + "  " + manageActivity.tempLink.getGoodchildtype());
        textObject.setText(manageActivity.tempLink.getCarno());
        textBoxNo.setText(manageActivity.box.getBoxno());
        if (manageActivity.box.getBoxtype().equals("lock")) {
            textSealTitle.setText("电子锁：");
        }
        manageActivity.mainApp.locationText = textAddress;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_READ_UID && resultCode == Activity.RESULT_OK) {
            rfid = data.getStringExtra(Constants.read_uid);
            if (!manageActivity.box.getBoxtype().equals("lock")) {
                textSeal.setText(rfid);
                return;
            }
            checkLock(rfid);
        } else if (requestCode == REQUEST_PHOTO_CAMERA && resultCode == Activity.RESULT_OK) {
            File file = new File(Constants.getStoragePath(), picName);
            manageActivity.dealWithPicture(imagePicture, imageDelete, file);
        } else if (requestCode == REQUEST_PASSWORD && requestCode == Activity.RESULT_OK) {
            textSeal.setText(rfid);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (TextUtils.isEmpty(textSeal.getText().toString())
                        || TextUtils.isEmpty((CharSequence) imagePicture.getTag())) {
                    Utils.showLongToast(R.string.incomplete_information, getContext());
                    return true;
                }
                sealTag();
                return true;
            }
        });
    }

    private void checkLock(final String rfid) {
        manageActivity.cancelConnectionTask();
        HashMap<String, String> map = manageActivity.getDefaultMap();
        map.put("sealrfid", rfid);
        manageActivity.task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                manageActivity.progressDialog.setMessage("检查电子锁信息");
                manageActivity.progressDialog.show();
            }

            @Override
            public void sendFinish() {
                manageActivity.progressDialog.dismiss();
            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                JSONModel.ReturnObject returnObject = manageActivity.gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(), getContext());
                    return;
                }
                JSONModel.Lock lock = manageActivity.gson.fromJson(returnObject.getM_ReturnOBJJsonObject(), JSONModel.Lock.class);
                if (!lock.isNewPwd()) {
                    textSeal.setText(rfid);
                }
                showOpenDialog(lock);
            }

            @Override
            public void sendLost(String result) {
                manageActivity.loginAgain();
            }
        });
        manageActivity.task.execute(Constants.check_new_lock);
    }

    private void showOpenDialog(final JSONModel.Lock lock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.notice);
        if (lock.isNewPwd())
            builder.setMessage("新锁，请前往修改初始密码");
        else
            builder.setMessage("是否开锁？");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(), NfcActivity.class);
                intent.putExtra(Constants.lock, lock);
                if (lock.isNewPwd()) {
                    intent.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_PASSWORD);
                    startActivityForResult(intent, REQUEST_PASSWORD);
                } else {
                    intent.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_OPEN);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void sealTag() {
        manageActivity.cancelConnectionTask();
        final HashMap<String, String> map = manageActivity.getDefaultMap();
        map.put("boxno", manageActivity.box.getBoxno());
        map.put("sealrfid", textSeal.getText().toString());
        if (manageActivity.mainApp.bdLocation != null) {
            map.put("addr", manageActivity.mainApp.bdLocation.getAddrStr());
            map.put("lat", String.valueOf(manageActivity.mainApp.bdLocation.getLatitude()));
            map.put("lng", String.valueOf(manageActivity.mainApp.bdLocation.getLongitude()));
        } else {
            map.put("addr", "未获取定位信息");
        }
//        HashMap<String, File> fileHashMap = new HashMap<>();
//        File file = new File(Constants.getStoragePath(), picName);
//        fileHashMap.put("pic", file);
        manageActivity.task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                manageActivity.progressDialog.setMessage(getString(R.string.submit_load));
                manageActivity.progressDialog.show();
            }

            @Override
            public void sendFinish() {
                manageActivity.progressDialog.dismiss();
            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                JSONModel.ReturnObject returnObject = manageActivity.gson.fromJson(result, JSONModel.ReturnObject.class);
                Utils.showLongToast(returnObject.getsMsg(), getContext());
                if (!returnObject.isbOK()) {
                    return;
                }
                File file = new File(Constants.getStoragePath(), picName);
                ContentValues values = new ContentValues();
                values.put("boxno", manageActivity.box.getBoxno());
                values.put("linkuuid", manageActivity.box.getLinkuuid());
                values.put("file", file.getAbsolutePath());
                values.put("sealOropen", "seal");
                manageActivity.sqLiteManage.insertPicture(values);
                Bundle bundle = new Bundle();
                bundle.putString("LGKey", manageActivity.userInfo.getLGKey());
                bundle.putString("boxno", manageActivity.box.getBoxno());
                bundle.putString("linkuuid", manageActivity.box.getLinkuuid());
                bundle.putString("file", file.getAbsolutePath());
                bundle.putString("sealOropen", "seal");
                SyncService.startActionNow(getContext(), bundle);

                manageActivity.finish();
            }

            @Override
            public void sendLost(String result) {

            }
        });
        manageActivity.task.execute(Constants.seal_tag);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.text_seal, R.id.image_picture, R.id.image_delete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_seal:
                Intent intent_nfc = new Intent(getContext(), NfcActivity.class);
                intent_nfc.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_READ_UID);
                startActivityForResult(intent_nfc, REQUEST_READ_UID);
                break;
            case R.id.image_picture:
                if (!TextUtils.isEmpty((String) imagePicture.getTag())) {
                    Intent intent = new Intent(getContext(), ShowPictureActivity.class);
                    intent.putExtra(Constants.picture_file, (String) imagePicture.getTag());
                    startActivity(intent);
                    return;
                }
                picName = Utils.createFileName();
                File file1 = new File(Constants.getStoragePath());
                if (!file1.exists()) {
                    file1.mkdirs();
                }
                File file = new File(file1, picName);
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent, REQUEST_PHOTO_CAMERA);
                break;
            case R.id.image_delete:
                imagePicture.setImageResource(R.drawable.image_manage_picture);
                imagePicture.setTag("");
                imageDelete.setVisibility(View.GONE);
                break;
        }
    }


}