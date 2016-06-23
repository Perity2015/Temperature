package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.ShowPictureActivity;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.service.SyncService;
import com.huiwu.temperaturecontrol.sqlite.bean.Picture;
import com.lzy.okhttputils.request.BaseRequest;

import java.io.File;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class OpenFragment extends ManageFragment {
    @Bind(R.id.text_address)
    TextView textAddress;
    @Bind(R.id.text_box_no)
    TextView textBoxNo;
    @Bind(R.id.image_picture)
    ImageView imagePicture;
    @Bind(R.id.image_delete)
    ImageView imageDelete;
    @Bind(R.id.text_exception)
    EditText textException;
    @Bind(R.id.row_exception)
    TableRow rowException;
    @Bind(R.id.check_exception)
    CheckBox checkException;

    private final int REQUEST_READ_UID = 101;
    private final int REQUEST_PHOTO_CAMERA = 102;
    private final int REQUEST_PICTURE_PREVIEW = 103;
    @Bind(R.id.text_seal_title)
    TextView textSealTitle;
    @Bind(R.id.text_seal)
    TextView textSeal;
    @Bind(R.id.text_goods)
    TextView textGoods;
    @Bind(R.id.text_object)
    TextView textObject;

    private String picName;

    public OpenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_open, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textAddress.requestFocus();
        textBoxNo.setText(manageActivity.box.getBoxno());
        textGoods.setText(manageActivity.tempLink.getGoodtype() + "  " + manageActivity.tempLink.getGoodchildtype());
        textObject.setText(manageActivity.tempLink.getCarno());
        textSeal.setText(manageActivity.tempLink.getSealrfid());
        if (manageActivity.box.getBoxtype().toUpperCase().equals("LOCK")) {
            textSealTitle.setText(R.string.e_lock);
        }

        mainApp.locationText = textAddress;

        checkException.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rowException.setVisibility(View.VISIBLE);
                } else {
                    rowException.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO_CAMERA && resultCode == Activity.RESULT_OK) {
            File file = new File(Constants.getStoragePath(), picName);
            manageActivity.dealWithPicture(imagePicture, imageDelete, file);
        } else if (requestCode == REQUEST_READ_UID && resultCode == Activity.RESULT_OK) {
            textSeal.setText(data.getStringExtra(Constants.READ_UID));
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
                if (TextUtils.isEmpty((CharSequence) imagePicture.getTag())) {
                    Utils.showLongToast(R.string.incomplete_information, getContext());
                    return true;
                }
                if (checkException.isChecked()) {
                    if (TextUtils.isEmpty(textException.getText().toString().trim())) {
                        Utils.showLongToast(R.string.incomplete_information, getContext());
                        return true;
                    }
                } else {
                    if (TextUtils.isEmpty(textSeal.getText().toString())) {
                        Utils.showLongToast(R.string.incomplete_information, getContext());
                        return true;
                    }
                }
                openTag();
                return true;
            }
        });
    }

    private void openTag() {
        final HashMap<String, String> map = baseActivity.getDefaultMap();
        map.put("boxno", manageActivity.box.getBoxno());
        if (mainApp.bdLocation != null) {
            map.put("addr", mainApp.bdLocation.getAddress());
            map.put("lat", String.valueOf(mainApp.bdLocation.getLatitude()));
            map.put("lng", String.valueOf(mainApp.bdLocation.getLongitude()));
        } else {
            map.put("addr", getString(R.string.no_location_address));
        }
        map.put("iserror", String.valueOf(checkException.isChecked()));
        map.put("errormsg", textException.getText().toString().trim());
        map.put("sealrfid", textSeal.getText().toString());
        ConnectionUtil.postParams(Constants.HOST + Constants.OPEN_TAG, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage(getString(R.string.submit_load));
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = gson.fromJson(s, JSONModel.ReturnObject.class);
                Utils.showLongToast(returnObject.getsMsg(), getContext());
                if (!returnObject.isbOK()) {
                    return;
                }
                File file = new File(Constants.getStoragePath(), picName);
                Picture picture = new Picture();
                picture.setBoxno(manageActivity.box.getBoxno());
                picture.setLinkuuid(manageActivity.box.getLinkuuid());
                picture.setFile(file.getAbsolutePath());
                picture.setSealOropen("open");
                sqLiteManage.insertPicture(mainApp.getDaoSession(), picture);

                HashMap<String, String> hashMap = baseActivity.getDefaultMap();
                hashMap.put("boxno", manageActivity.box.getBoxno());
                hashMap.put("linkuuid", manageActivity.box.getLinkuuid());
                hashMap.put("sealOropen", "open");

                HashMap<String, File> fileHashMap = new HashMap<>();
                fileHashMap.put("file", file);

                SyncService.startActionNow(getContext(), hashMap, fileHashMap);


                if (manageActivity.box.getBoxtype().toUpperCase().equals("LOCK")) {
                    JSONModel.Lock lock = gson.fromJson(returnObject.getM_ReturnOBJJsonObject(), JSONModel.Lock.class);
                    Intent intent = new Intent(getContext(), NfcActivity.class);
                    intent.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_OPEN);
                    intent.putExtra(Constants.LOCK, lock);
                    startActivity(intent);
                }
                manageActivity.finish();
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, getContext());
            }

            @Override
            public void onLost() {
                manageActivity.loginAgain();
            }

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.image_picture, R.id.image_delete, R.id.text_seal})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_picture:
                if (!TextUtils.isEmpty((String) imagePicture.getTag())) {
                    Intent intent = new Intent(getContext(), ShowPictureActivity.class);
                    intent.putExtra(Constants.PICTURE_FILE, (String) imagePicture.getTag());
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
                imagePicture.setImageResource(R.drawable.ic_manage_picture);
                imagePicture.setTag("");
                imageDelete.setVisibility(View.GONE);
                break;
            case R.id.text_seal:
                Intent intent_nfc = new Intent(getContext(), NfcActivity.class);
                intent_nfc.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_READ_UID);
                startActivityForResult(intent_nfc, REQUEST_READ_UID);
                break;
        }
    }

}
