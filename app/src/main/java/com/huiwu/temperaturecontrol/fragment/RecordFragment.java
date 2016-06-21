package com.huiwu.temperaturecontrol.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.ChartActivity;
import com.huiwu.temperaturecontrol.NetRecordsActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.sqlite.bean.GoodsType;
import com.huiwu.temperaturecontrol.sqlite.bean.TagInfo;
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
public class RecordFragment extends BaseFragment {

    @Bind(R.id.recyclerView_Records)
    RecyclerView recyclerViewRecords;
    @Bind(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;

    private LinearLayoutManager mLayoutManager;

    private ArrayList<TagInfo> records = new ArrayList<>();

    private long index = Integer.MAX_VALUE;

    private TagInfoAdapter adapter;


    public RecordFragment() {
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
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewRecords.setLayoutManager(mLayoutManager);

        recyclerViewRecords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (index == 0) {
                    return;
                }
                int position = mLayoutManager.findLastCompletelyVisibleItemPosition();
                swipeLayout.setEnabled(mLayoutManager.findFirstVisibleItemPosition() == 0);
                if (position == records.size() - 1) {
                    ArrayList<TagInfo> tagInfos = sqLiteManage.getConfigTagInfos(mainApp.getDaoSession(), index);
                    if (tagInfos.size() < 10) {
                        index = 0;
                    } else {
                        index = tagInfos.get(tagInfos.size() - 1).getId();
                    }
                    records.addAll(tagInfos);
                    if (adapter == null) {
                        adapter = new TagInfoAdapter();
                        recyclerViewRecords.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                initData();
            }
        });

        initData();
    }

    public void initData() {
        records.clear();
        index = Integer.MAX_VALUE;
        ArrayList<TagInfo> tagInfos = sqLiteManage.getConfigTagInfos(mainApp.getDaoSession(), index);
        if (tagInfos.size() < 10) {
            index = 0;
        } else {
            index = tagInfos.get(tagInfos.size() - 1).getId();
        }
        records.addAll(tagInfos);
        if (adapter == null) {
            adapter = new TagInfoAdapter();
            recyclerViewRecords.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_records, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(getContext(), NetRecordsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }



    private class TagInfoAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_record_item, parent, false);
            TagInfoHolder holder = new TagInfoHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            TagInfoHolder holder = (TagInfoHolder) viewHolder;
            final TagInfo tagInfo = records.get(position);
            holder.textRecordItemObject.setText(tagInfo.getObject());
            JSONModel.Box box = gson.fromJson(tagInfo.getBox(), JSONModel.Box.class);
            holder.textRecordItemBox.setText(box.getBoxno());

            GoodsType goodsType = gson.fromJson(tagInfo.getGoods(), GoodsType.class);
            holder.textRecordItemGoods.setText(goodsType.getParentgoodtype() + "  " + goodsType.getGoodtype());

            holder.imageRecordItemUpload.setVisibility(tagInfo.isHavepost() ? View.INVISIBLE : View.VISIBLE);
            holder.btnRecordItemTime.setText(DateFormat.format("MM-dd kk:mm:ss", tagInfo.getReadTime()));
            holder.btnRecordItemTemp.setText(tagInfo.getTempMin() + "℃ - " + tagInfo.getTempMax() + "℃");
            holder.btnRecordItemHum.setText(tagInfo.getHumMin() + "% - " + tagInfo.getHumMax() + "%");

            if (tagInfo.isJustTemp()) {
                holder.imageRecordItemHum.setVisibility(View.GONE);
                holder.btnRecordItemHum.setVisibility(View.GONE);
            } else {
                holder.imageRecordItemHum.setVisibility(View.VISIBLE);
                holder.btnRecordItemHum.setVisibility(View.VISIBLE);
            }

            holder.layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ChartActivity.class);
                    intent.putExtra(Constants.TAG_INFO, tagInfo);
                    startActivity(intent);
                }
            });

            holder.imageRecordItemUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadData(tagInfo);
                }
            });
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

    }

    static class TagInfoHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_record_item_object)
        TextView textRecordItemObject;
        @Bind(R.id.text_record_item_box)
        TextView textRecordItemBox;
        @Bind(R.id.text_record_item_goods)
        TextView textRecordItemGoods;
        @Bind(R.id.image_record_item_upload)
        ImageView imageRecordItemUpload;
        @Bind(R.id.btn_record_item_time)
        Button btnRecordItemTime;
        @Bind(R.id.btn_record_item_hum)
        Button btnRecordItemHum;
        @Bind(R.id.image_record_item_hum)
        ImageView imageRecordItemHum;
        @Bind(R.id.btn_record_item_temp)
        Button btnRecordItemTemp;
        @Bind(R.id.layout_item)
        CardView layoutItem;

        public TagInfoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void uploadData(final TagInfo tagInfo) {
        HashMap<String, String> map = baseActivity.getDefaultMap();
        if (mainApp.bdLocation != null) {
            map.put("address", mainApp.bdLocation.getAddress());
        } else {
            map.put("address", "为获取定位信息");
        }
        map.put("dataarray", tagInfo.getDataarray());
        if (!tagInfo.isJustTemp()) {
            map.put("humidityArray", tagInfo.getHumidityArray());
        }
        map.put("m_begintime", DateFormat.format("yyyy-MM-dd kk:mm:ss", tagInfo.getStartTime()).toString());
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("rfid", tagInfo.getUid());
        map.put("bover", String.valueOf(false));
        map.put("roundCircle", String.valueOf(tagInfo.getRoundCircle()));
        map.put("index", String.valueOf(tagInfo.getNumber()));

        ConnectionUtil.postParams(Constants.UPLOAD_DATA_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage(getString(R.string.upload_info_load));
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = gson.fromJson(s, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    uploadOfflineData(tagInfo);
                    return;
                }
                tagInfo.setHavepost(true);
                adapter.notifyDataSetChanged();
                sqLiteManage.updateConfigTagInfoStatus(mainApp.getDaoSession(), tagInfo);
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, getContext());
            }

            @Override
            public void onLost() {
                baseActivity.loginAgain();
            }


        });
    }

    private void uploadOfflineData(final TagInfo tagInfo) {
        HashMap<String, String> map = baseActivity.getDefaultMap();
        if (mainApp.bdLocation != null) {
            map.put("address", mainApp.bdLocation.getAddress());
        } else {
            map.put("address", getString(R.string.no_location_address));
        }
        map.put("dataarray", tagInfo.getDataarray());
        if (!tagInfo.isJustTemp()) {
            map.put("humidityArray", tagInfo.getHumidityArray());
        }
        map.put("rfid", tagInfo.getUid());
        map.put("bover", String.valueOf(false));
        map.put("begintime", DateFormat.format("yyyy-MM-dd kk:mm:ss", tagInfo.getStartTime()).toString());
        map.put("roundCircle", String.valueOf(tagInfo.getRoundCircle()));
        map.put("index", String.valueOf(tagInfo.getNumber()));
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("createtime", Utils.formatDateTimeOffLine(tagInfo.getReadTime()));


        ConnectionUtil.postParams(Constants.UPLOAD_DATA_OFFLINE_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage(getString(R.string.upload_info_load));
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                tagInfo.setHavepost(true);
                sqLiteManage.updateConfigTagInfoStatus(mainApp.getDaoSession(), tagInfo);
                adapter.notifyDataSetChanged();
                JSONModel.ReturnObject returnObject = gson.fromJson(s, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(), getContext());
                    return;
                }
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, getContext());
            }

            @Override
            public void onLost() {
                baseActivity.loginAgain();
            }


        });
    }
}
