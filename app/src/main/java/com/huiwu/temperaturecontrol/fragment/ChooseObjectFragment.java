package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    private int selectId = -1;

    private RfidGood[] allObjects;
    private List<RfidGood> searchObjects = new ArrayList<>();
    private ObjectAdapter adapter;

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
                if (selectId == -1) {
                    return true;
                }
                RfidGood rfidGood = searchObjects.get(selectId);
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
        ConnectionUtil.postParams(Constants.GET_ALL_OBJECTS_URL, map, new StringConnectionCallBack() {
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
                searchObjects.clear();
                for (RfidGood rfidGood : allObjects) {
                    searchObjects.add(rfidGood);
                }

                selectId = -1;
                if (adapter == null) {
                    adapter = new ObjectAdapter(getContext(), R.layout.layout_parent_goods_item, searchObjects);
                    listViewObjects.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
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

    private class ObjectAdapter extends ArrayAdapter {

        public ObjectAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_parent_goods_item, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(searchObjects.get(position).getRfidgoodname());
            if (selectId == position) {
                textView.setTextColor(Color.RED);
            } else {
                textView.setTextColor(Color.parseColor("#333333"));
            }
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectId = position;
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }
}
