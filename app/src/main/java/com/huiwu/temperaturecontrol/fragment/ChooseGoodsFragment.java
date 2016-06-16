package com.huiwu.temperaturecontrol.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.ChooseActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.sqlite.bean.GoodsType;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseGoodsFragment extends Fragment {


    @Bind(R.id.expandable_ListView_goods)
    ExpandableListView expandableListViewGoods;

    private JSONModel.Goods[] parentGoods;
    private ArrayList<JSONModel.Goods[]> allGoods;

    private ChooseActivity chooseActivity;
    private JSONModel.UserInfo userInfo;

    private GoodsAdapter adapter;

    private int gId = -1;
    private int cId = -1;

    public ChooseGoodsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_choose_goods, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expandableListViewGoods.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (allGoods.get(groupPosition).length == 0) {
                    getChildGoods(parentGoods[groupPosition].getId(), groupPosition);
                }
                return false;
            }
        });

        expandableListViewGoods.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < parentGoods.length; i++) {
                    if (i != groupPosition && expandableListViewGoods.isGroupExpanded(groupPosition)) {
                        expandableListViewGoods.collapseGroup(i);
                    }
                }
            }
        });
        expandableListViewGoods.setGroupIndicator(null);

        getParentsGoods();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (gId == -1 && cId == -1) {
                    Utils.showLongToast("请选择一个商品类型", getContext());
                    return true;
                }
                JSONModel.Goods goods = allGoods.get(gId)[cId];
                Intent intent = new Intent();
                intent.putExtra(Constants.select_object, goods);
                chooseActivity.setResult(Activity.RESULT_OK, intent);
                chooseActivity.finish();
                return true;
            }
        });
    }

    private void getParentsGoods() {
        HashMap<String, String> map = chooseActivity.getDefaultMap();
        ConnectionUtil.postParams(Constants.get_parent_goods_url, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                chooseActivity.progressDialog.setMessage("加载信息");
                chooseActivity.progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                chooseActivity.progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = chooseActivity.gson.fromJson(s, JSONModel.ReturnObject.class);
                parentGoods = chooseActivity.gson.fromJson(returnObject.getM_ReturnOBJJsonArray(), JSONModel.Goods[].class);
                GoodsType[] goodsTypes = chooseActivity.gson.fromJson(returnObject.getM_ReturnOBJJsonArray(), GoodsType[].class);
                chooseActivity.sqLiteManage.insertGoodsTypes(chooseActivity.mainApp.daoMaster.newSession(), goodsTypes);
                allGoods = new ArrayList<>();
                for (int i = 0; i < parentGoods.length; i++) {
                    allGoods.add(new JSONModel.Goods[0]);
                }
                adapter = new GoodsAdapter();
                expandableListViewGoods.setAdapter(adapter);
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, getContext());
            }

            @Override
            public void onLost() {
                chooseActivity.loginAgain();
            }

        });
    }

    private void getChildGoods(int id, final int groupPosition) {
        HashMap<String, String> map = chooseActivity.getDefaultMap();
        map.put("pid", String.valueOf(id));
        ConnectionUtil.postParams(Constants.get_child_goods_url, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {

            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {

            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnData returnData = chooseActivity.gson.fromJson(s, JSONModel.ReturnData.class);
                JSONModel.Goods[] Goods = chooseActivity.gson.fromJson(returnData.getData(), JSONModel.Goods[].class);
                GoodsType[] goodsTypes = chooseActivity.gson.fromJson(returnData.getData(), GoodsType[].class);
                chooseActivity.sqLiteManage.insertGoodsTypes(chooseActivity.mainApp.daoMaster.newSession(), goodsTypes);
                allGoods.set(groupPosition, Goods);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onParseFailed(@Nullable Response response) {

            }

            @Override
            public void onLost() {
                chooseActivity.loginAgain();
            }


        });
    }

    private class GoodsAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return allGoods.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return allGoods.get(groupPosition).length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_parent_goods_item, parent, false);
            }
            ((TextView) convertView).setText(parentGoods[groupPosition].getGoodtype());
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_child_goods_item, parent, false);
            }
            TextView textView = (TextView) convertView;
            if (gId == groupPosition && cId == childPosition) {
                textView.setTextColor(Color.RED);
            } else {
                textView.setTextColor(Color.parseColor("#666666"));
            }
            textView.setText(allGoods.get(groupPosition)[childPosition].getGoodtype());

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gId = groupPosition;
                    cId = childPosition;
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}