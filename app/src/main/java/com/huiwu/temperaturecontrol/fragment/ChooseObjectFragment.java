package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.temperaturecontrol.ChooseActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseObjectFragment extends Fragment {


    @Bind(R.id.text_object)
    EditText textObject;
    @Bind(R.id.listView_objects)
    ListView listViewObjects;

    private int selectId = -1;

    private ChooseActivity chooseActivity;
    private JSONModel.UserInfo userInfo;

    private ArrayList<JSONModel.RfidGood> allObjects = new ArrayList<>();
    private ArrayList<JSONModel.RfidGood> searchObjects = new ArrayList<>();
    private ObjectAdapter adapter;

    public ChooseObjectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        chooseActivity = (ChooseActivity) getActivity();
        userInfo = chooseActivity.userInfo;
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

            }

            @Override
            public void afterTextChanged(Editable s) {
                String object = s.toString().trim();
                if (TextUtils.isEmpty(object)) {
                    searchObjects = allObjects;
                } else {
                    searchObjects.clear();
                    for (JSONModel.RfidGood rfidGood : allObjects) {
                        if (rfidGood.getRfidgoodname().toUpperCase().contains(object.toUpperCase())) {
                            searchObjects.add(rfidGood);

                        }
                    }
                }
//                adapter = new ObjectAdapter();
//                listViewObjects.setAdapter(adapter);

                selectId = -1;
                adapter = new ObjectAdapter();
                listViewObjects.setAdapter(adapter);
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
                JSONModel.RfidGood rfidGood = searchObjects.get(selectId);
                Intent intent = new Intent();
                intent.putExtra(Constants.select_object, rfidGood);
                chooseActivity.setResult(Activity.RESULT_OK, intent);
                chooseActivity.finish();
                return true;
            }
        });
    }

    private void getAllObjects() {
        chooseActivity.cancelConnectionTask();
        HashMap<String, String> map = chooseActivity.getDefaultMap();
        chooseActivity.task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {

            }

            @Override
            public void sendFinish() {

            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                JSONModel.ReturnData returnData = chooseActivity.gson.fromJson(result, JSONModel.ReturnData.class);
                JSONModel.RfidGood[] tempObjects = chooseActivity.gson.fromJson(returnData.getRows(), JSONModel.RfidGood[].class);
                Arrays.sort(tempObjects);
                searchObjects.clear();
                for (JSONModel.RfidGood rfidGood : tempObjects) {
                    allObjects.add(rfidGood);
                    searchObjects.add(rfidGood);
                }
                selectId = -1;
                if (adapter == null) {
                    adapter = new ObjectAdapter();
                    listViewObjects.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void sendLost(String result) {

            }
        });
        chooseActivity.task.execute(Constants.get_all_objects_url);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private class ObjectAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchObjects.size();
        }

        @Override
        public Object getItem(int position) {
            return searchObjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
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
