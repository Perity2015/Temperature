package com.huiwu.temperaturecontrol.fragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.huiwu.temperaturecontrol.MainActivity;
import com.huiwu.temperaturecontrol.NetRecordsActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.sqlite.SQLiteManage;
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
public class RecordFragment extends Fragment {

    @Bind(R.id.recyclerView_Records)
    RecyclerView recyclerViewRecords;
    @Bind(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;

    private LinearLayoutManager mLayoutManager;

    private MainActivity mainActivity;

    private int index;

    private long load_time;

    private ArrayList<JSONModel.TagInfo> records = new ArrayList<>();

    private TagInfoAdapter adapter;

    Handler mhandler = new Handler();

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
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
                    if (System.currentTimeMillis() - load_time > 2000) {
                        new LoadRecordsTask().execute(index);
                    }
                }
            }
        });
//        swipeLayout.setColorSchemeColors(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
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
        new LoadRecordsTask().execute(Integer.MAX_VALUE);
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

    private class LoadRecordsTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (load_time != 0) {
                mainActivity.progressDialog.setMessage(getString(R.string.continue_load_data));
                mainActivity.progressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            HashMap<String, Object> map = mainActivity.sqLiteManage.getDefaultSearchRecords(params[0]);
            if (map != null) {
                ArrayList<JSONModel.TagInfo> temp = (ArrayList<JSONModel.TagInfo>) map.get(SQLiteManage.RECORDS);
                records.addAll(temp);
                index = (int) map.get(SQLiteManage.RECORDS_INDEX);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            load_time = System.currentTimeMillis();
            if (mainActivity.progressDialog.isShowing())
                mainActivity.progressDialog.dismiss();
            if (adapter == null) {
                adapter = new TagInfoAdapter();
                recyclerViewRecords.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
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
            final JSONModel.TagInfo tagInfo = records.get(position);
            holder.textRecordItemObject.setText(tagInfo.getObject());
            if (tagInfo.getBox() != null) {
                holder.textRecordItemBox.setText(tagInfo.getBox().getBoxno());
            }
            if (tagInfo.getGoods() != null) {
                holder.textRecordItemGoods.setText(tagInfo.getGoods().getParentgoodtype() + "  " + tagInfo.getGoods().getGoodtype());
            }
            holder.imageRecordItemUpload.setVisibility(tagInfo.isHavepost() ? View.INVISIBLE : View.VISIBLE);
            holder.btnRecordItemTime.setText(DateFormat.format("MM-dd kk:mm:ss", tagInfo.getReadTime()));
            holder.btnRecordItemTemp.setText(tagInfo.getTemp_min() + "℃ - " + tagInfo.getTemp_max() + "℃");
            holder.btnRecordItemHum.setText(tagInfo.getHum_min() + "% - " + tagInfo.getHum_max() + "%");

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
                    intent.putExtra(Constants.tag_info, tagInfo);
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

    private void uploadData(final JSONModel.TagInfo tagInfo) {
        HashMap<String, String> map = mainActivity.getDefaultMap();
        if (mainActivity.mainApp.bdLocation != null) {
            map.put("address", mainActivity.mainApp.bdLocation.getAddress());
        } else {
            map.put("address", "为获取定位信息");
        }
        map.put("dataarray", mainActivity.gson.toJson(tagInfo.getTempList()));
        if (!tagInfo.isJustTemp()) {
            map.put("humidityArray", mainActivity.gson.toJson(tagInfo.getHumList()));
        }
        map.put("m_begintime", DateFormat.format("yyyy-MM-dd kk:mm:ss", tagInfo.getStartTime()).toString());
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("rfid", tagInfo.getUid());
        map.put("bover", String.valueOf(false));
        map.put("roundCircle", String.valueOf(tagInfo.getRoundCircle()));
        map.put("index", String.valueOf(tagInfo.getIndex()));

        ConnectionUtil.postParams(Constants.upload_data_url, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                mainActivity.progressDialog.setMessage("上传记录信息中……");
                mainActivity.progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                mainActivity.progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnObject returnObject = mainActivity.gson.fromJson(s, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    uploadOfflineData(tagInfo);
                    return;
                }
                tagInfo.setHavepost(true);
                adapter.notifyDataSetChanged();
                mainActivity.sqLiteManage.changeRecordStatus(tagInfo);
            }

            @Override
            public void onParseFailed(@Nullable Response response) {
                Utils.showLongToast(R.string.net_error, getContext());
            }

            @Override
            public void onLost() {
                mainActivity.loginAgain();
            }


        });
    }

    private void uploadOfflineData(final JSONModel.TagInfo tagInfo) {
        HashMap<String, String> map = mainActivity.getDefaultMap();
        if (mainActivity.mainApp.bdLocation != null) {
            map.put("address", mainActivity.mainApp.bdLocation.getAddress());
        } else {
            map.put("address", "为获取定位信息");
        }
        map.put("dataarray", mainActivity.gson.toJson(tagInfo.getTempList()));
        if (!tagInfo.isJustTemp()) {
            map.put("humidityArray", mainActivity.gson.toJson(tagInfo.getHumList()));
        }
        map.put("rfid", tagInfo.getUid());
        map.put("bover", String.valueOf(false));
        map.put("begintime", DateFormat.format("yyyy-MM-dd kk:mm:ss", tagInfo.getStartTime()).toString());
        map.put("roundCircle", String.valueOf(tagInfo.getRoundCircle()));
        map.put("index", String.valueOf(tagInfo.getIndex()));
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("createtime", Utils.formatDateTimeOffLine(tagInfo.getReadTime()));


        ConnectionUtil.postParams(Constants.upload_data_offline_url, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                mainActivity.progressDialog.setMessage("上传记录信息中……");
                mainActivity.progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                mainActivity.progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                tagInfo.setHavepost(true);
                mainActivity.sqLiteManage.changeRecordStatus(tagInfo);
                adapter.notifyDataSetChanged();
                JSONModel.ReturnObject returnObject = mainActivity.gson.fromJson(s, JSONModel.ReturnObject.class);
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
                mainActivity.loginAgain();
            }


        });
    }
}
