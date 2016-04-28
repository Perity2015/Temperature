package com.huiwu.temperaturecontrol;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.google.gson.JsonObject;
import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.bean.TLog;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilterActivity extends BaseActivity {

    @Bind(R.id.radioGroup)
    RadioGroup radioGroup;
    @Bind(R.id.listView_goods)
    ListView listViewGoods;
    @Bind(R.id.listView_boxes)
    ListView listViewBoxes;
    @Bind(R.id.listView_objects)
    ListView listViewObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        cancelConnectionTask();
        HashMap<String, String> map = getDefaultMap();
        map.put("companyid", String.valueOf(userInfo.getUserPower().getCompanyid()));
        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage("加载数据");
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
                TLog.d("DEBUG", result);
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                JsonObject jsonObject = returnObject.getM_ReturnOBJJsonObject();
                JSONModel.Box[] boxes = gson.fromJson(jsonObject.getAsJsonArray("boxes"), JSONModel.Box[].class);
                listViewBoxes.setAdapter(new ArrayAdapter<>(mContext, R.layout.layout_parent_goods_item, boxes));
            }

            @Override
            public void sendLost(String result) {

            }
        });
        task.execute(Constants.get_data_url);
    }
}
