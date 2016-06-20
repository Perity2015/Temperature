package com.huiwu.temperaturecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.google.gson.JsonObject;
import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

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
        HashMap<String, String> map = getDefaultMap();
        map.put("companyid", String.valueOf(userInfo.getUserPower().getCompanyid()));
        ConnectionUtil.postParams(Constants.GET_DATA_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage("加载数据");
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = gson.fromJson(s, JSONModel.ReturnObject.class);
                JsonObject jsonObject = returnObject.getM_ReturnOBJJsonObject();
                JSONModel.Box[] boxes = gson.fromJson(jsonObject.getAsJsonArray("boxes"), JSONModel.Box[].class);
                listViewBoxes.setAdapter(new ArrayAdapter<>(mContext, R.layout.layout_parent_goods_item, boxes));
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, mContext);
            }

            @Override
            public void onLost() {
                loginAgain();
            }

        });
    }
}
