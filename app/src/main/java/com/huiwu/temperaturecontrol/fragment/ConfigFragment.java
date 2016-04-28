package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.huiwu.model.view.MyAlertDialog;
import com.huiwu.temperaturecontrol.ChooseActivity;
import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.NfcActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragment extends Fragment {


    @Bind(R.id.text_box_no)
    TextView textBoxNo;
    @Bind(R.id.text_goods)
    TextView textGoods;
    @Bind(R.id.text_sample_info)
    TextView textSampleInfo;
    @Bind(R.id.text_object)
    TextView textObject;
    @Bind(R.id.seekBar_time)
    SeekBar seekBarTime;
    @Bind(R.id.text_delay_time)
    TextView textDelayTime;
    @Bind(R.id.text_config_notice)
    TextView textConfigNotice;

    private ManageActivity manageActivity;

    private JSONModel.UserInfo userInfo;

    private final int REQUEST_GOODS = 201;
    private final int REQUEST_OBJECT = 202;
    private final int REQUEST_CONFIG = 203;

    private JSONModel.Goods selectGoods;
    private JSONModel.RfidGood selectRfidGoods;

    private JSONModel.TagInfo tagInfo;

    public ConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        manageActivity = (ManageActivity) getActivity();
        userInfo = manageActivity.userInfo;
        tagInfo = new JSONModel.TagInfo();

        manageActivity.tempLink = new JSONModel.TempLink();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textConfigNotice.requestFocus();
        textBoxNo.setText(manageActivity.box.getBoxno());
        tagInfo.setBox(manageActivity.box);

        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int delayTime = 1;
                if (progress < 5) {
                    delayTime = progress + 1;
                } else {
                    delayTime = (progress - 3) * 5;
                }
                tagInfo.setDelayTime(delayTime);
                textDelayTime.setText(String.format("%02d", delayTime));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showConfigSelectDialog();
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_OBJECT) {
            selectRfidGoods = data.getParcelableExtra(Constants.select_object);
            textObject.setText(selectRfidGoods.getRfidgoodname());
            tagInfo.setObject(selectRfidGoods.getRfidgoodname());
            manageActivity.tempLink.setCarno(selectRfidGoods.getRfidgoodname());
        } else if (requestCode == REQUEST_GOODS) {
            selectGoods = data.getParcelableExtra(Constants.select_object);
            textGoods.setText(selectGoods.getParentgoodtype() + "    " + selectGoods.getGoodtype());

            manageActivity.tempLink.setGoodtype(selectGoods.getParentgoodtype());
            manageActivity.tempLink.setGoodchildtype(selectGoods.getGoodtype());

            tagInfo.setGoods(selectGoods);

            String sampleInfo = "采样间隔：" + selectGoods.getOnetime() + "分钟\r\n";
            sampleInfo += "温度上限：" + selectGoods.getHightmpnumber() + "℃\r\n";
            sampleInfo += "温度下限：" + selectGoods.getLowtmpnumber() + "℃\r\n";
            sampleInfo += "湿度上限：" + selectGoods.getHighhumiditynumber() + "%\r\n";
            sampleInfo += "湿度下限：" + selectGoods.getLowhumiditynumber() + "%";
            textSampleInfo.setLines(6);
            textSampleInfo.setText(sampleInfo);
        } else if (requestCode == REQUEST_CONFIG) {
            if (!TextUtils.equals("normal", manageActivity.box.getBoxtype())) {
                showFinishAddDialog();
            } else {
                manageActivity.finish();
            }
        }
    }

    private void showFinishAddDialog() {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setCancelable(false);
        builder.setMessage("配置成功，是否继续执行下一步？");
        builder.setPositiveButton("下一步", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                manageActivity.option = ManageActivity.OPTION_SEAL;
                manageActivity.setSelectFragment(manageActivity.option);
            }
        });
        builder.setNegativeButton("结束", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                manageActivity.finish();
            }
        });
        builder.show();
    }

    private void showConfigSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(getResources().getStringArray(R.array.selectConfigs), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(getContext(), NfcActivity.class);
                        intent.putExtra(Constants.tag_info, tagInfo);
                        intent.putExtra(NfcActivity.COMMAND_PARAM, NfcActivity.NFC_CONFIG);
                        startActivityForResult(intent, REQUEST_CONFIG);
                        break;
                    case 1:

                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.text_goods, R.id.text_object})
    public void onClick(View view) {
        Intent intent = new Intent(getContext(), ChooseActivity.class);
        int requestCode = 0;
        switch (view.getId()) {
            case R.id.text_goods:
                requestCode = REQUEST_GOODS;
                intent.putExtra(ChooseActivity.CHOOSE_FLAG, ChooseActivity.CHOOSE_GOODS);
                break;
            case R.id.text_object:
                intent.putExtra(ChooseActivity.CHOOSE_FLAG, ChooseActivity.CHOOSE_OBJECT);
                requestCode = REQUEST_OBJECT;
                break;
        }
        startActivityForResult(intent, requestCode);
    }
}
