package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.sqlite.bean.RfidGood;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseObjectFragment extends BaseFragment {


    @Bind(R.id.text_object)
    AutoCompleteTextView textObject;
    @Bind(R.id.listView_objects)
    ListView listViewObjects;

    private RfidGood[] allObjects;
    private List<RfidGood> searchObjects = new ArrayList<>();

    public ChooseObjectFragment() {
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
        View view = inflater.inflate(R.layout.fragment_choose_object, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textObject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String object = s.toString().trim();
                searchObjects.clear();
                if (allObjects == null) {
                    return;
                }
                for (RfidGood rfidGood : allObjects) {
                    if (rfidGood.getRfidgoodname().toUpperCase().contains(object.toUpperCase())) {
                        searchObjects.add(rfidGood);
                    }
                }

                ArrayAdapter arrayAdapter = new ArrayAdapter(mContext, R.layout.layout_parent_goods_item, searchObjects);
                textObject.setAdapter(arrayAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listViewObjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RfidGood rfidGood = allObjects[position];
                Intent intent = new Intent();
                intent.putExtra(Constants.SELECT_OBJECT, rfidGood.getRfidgoodname());
                baseActivity.setResult(Activity.RESULT_OK, intent);
                baseActivity.finish();
            }
        });

        getAllObjects();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                baseActivity.hideSoftInput();
                String rfidGood = textObject.getText().toString().trim();
                if (TextUtils.isEmpty(rfidGood)) {
                    return true;
                }
                Intent intent = new Intent();
                intent.putExtra(Constants.SELECT_OBJECT, rfidGood);
                baseActivity.setResult(Activity.RESULT_OK, intent);
                baseActivity.finish();
                return true;
            }
        });
    }

    private void getAllObjects() {
        HashMap<String, String> map = baseActivity.getDefaultMap();
        ConnectionUtil.postParams(Constants.HOST + Constants.GET_ALL_OBJECTS_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {

            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {

            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnData returnData = gson.fromJson(s, JSONModel.ReturnData.class);
                allObjects = gson.fromJson(returnData.getRows(), RfidGood[].class);
                sqLiteManage.insertRfidGoods(mainApp.daoMaster.newSession(), allObjects);
                Arrays.sort(allObjects);
                listViewObjects.setAdapter(new ArrayAdapter<>(mContext, R.layout.layout_parent_goods_item, allObjects));
            }

            @Override
            public void onParseFailed(@Nullable Response response) {

            }

            @Override
            public void onLost() {
                baseActivity.loginAgain();
            }

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
