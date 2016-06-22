package com.huiwu.temperaturecontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.sqlite.bean.GoodsType;
import com.huiwu.temperaturecontrol.sqlite.bean.TagInfo;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

public class NetRecordsActivity extends BaseActivity {
    @Bind(R.id.image_year_pre)
    ImageView imageYearPre;
    @Bind(R.id.text_year)
    TextView textYear;
    @Bind(R.id.image_year_next)
    ImageView imageYearNext;
    @Bind(R.id.seekBar_month)
    SeekBar seekBarMonth;
    @Bind(R.id.layout_month)
    RadioGroup layoutMonth;
    @Bind(R.id.recycler_links)
    RecyclerView recyclerView_links;

    private AlertDialog gatherRecordsDialog;

    private JSONModel.TempLink[] tempLinks;

    private LinkAdapter adapter;

    private int year, month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_records);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        recyclerView_links = (RecyclerView) findViewById(R.id.recycler_links);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView_links.setLayoutManager(mLayoutManager);

        layoutMonth.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int progress = 0;
                switch (checkedId) {
                    case R.id.radio_1:
                        progress = 0;
                        break;
                    case R.id.radio_2:
                        progress = 1;
                        break;
                    case R.id.radio_3:
                        progress = 2;
                        break;
                    case R.id.radio_4:
                        progress = 3;
                        break;
                    case R.id.radio_5:
                        progress = 4;
                        break;
                    case R.id.radio_6:
                        progress = 5;
                        break;
                    case R.id.radio_7:
                        progress = 6;
                        break;
                    case R.id.radio_8:
                        progress = 7;
                        break;
                    case R.id.radio_9:
                        progress = 8;
                        break;
                    case R.id.radio_10:
                        progress = 9;
                        break;
                    case R.id.radio_11:
                        progress = 10;
                        break;
                    case R.id.radio_12:
                        progress = 11;
                        break;
                }
                seekBarMonth.setProgress(progress);
                searchLinks();
            }
        });

        seekBarMonth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                month = progress;
                switch (progress) {
                    case 0:
                        layoutMonth.check(R.id.radio_1);
                        break;
                    case 1:
                        layoutMonth.check(R.id.radio_2);
                        break;
                    case 2:
                        layoutMonth.check(R.id.radio_3);
                        break;
                    case 3:
                        layoutMonth.check(R.id.radio_4);
                        break;
                    case 4:
                        layoutMonth.check(R.id.radio_5);
                        break;
                    case 5:
                        layoutMonth.check(R.id.radio_6);
                        break;
                    case 6:
                        layoutMonth.check(R.id.radio_7);
                        break;
                    case 7:
                        layoutMonth.check(R.id.radio_8);
                        break;
                    case 8:
                        layoutMonth.check(R.id.radio_9);
                        break;
                    case 9:
                        layoutMonth.check(R.id.radio_10);
                        break;
                    case 10:
                        layoutMonth.check(R.id.radio_11);
                        break;
                    case 11:
                        layoutMonth.check(R.id.radio_12);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        textYear.setText(String.valueOf(year));
        month = calendar.get(Calendar.MONTH);
        seekBarMonth.setProgress(month);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_net_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivityForResult(new Intent(mContext, FilterActivity.class), 2);
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchLinks() {

        final HashMap<String, String> map = getDefaultMap();
        map.put("companyid", String.valueOf(userInfo.getMessage().getCompanyid()));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, seekBarMonth.getProgress());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        map.put("begintime", DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar).toString());
        calendar.set(Calendar.MONTH, seekBarMonth.getProgress() + 1);
        map.put("endtime", DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar).toString());

        ConnectionUtil.postParams(Constants.HOST + Constants.GET_TEMPERATURE_LINKS_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage("加载信息");
                progressDialog.show();
            }

            @Override
            public void sendFinish(boolean b, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                progressDialog.dismiss();
            }

            @Override
            public void onParse(String s, Response response) {
                JSONModel.ReturnData returnData = gson.fromJson(s, JSONModel.ReturnData.class);
                if (returnData.getTotal() == 0) {
                    Utils.showLongToast("未查询到数据", mContext);
                }
                tempLinks = gson.fromJson(returnData.getRows(), JSONModel.TempLink[].class);
                if (adapter == null) {
                    adapter = new LinkAdapter();
                    recyclerView_links.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
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

    private void getGatherRecords(final JSONModel.TempLink tempLink) {
        HashMap<String, String> map = getDefaultMap();
        map.put("linkuuid", tempLink.getLinkuuid());
        ConnectionUtil.postParams(Constants.HOST + Constants.GET_GATHER_TEMPERATURE_RECORDS_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
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
                    return;
                }
                JSONModel.GatherRecord[] gatherRecords = gson.fromJson(returnObject.getM_ReturnOBJJsonArray(), JSONModel.GatherRecord[].class);
                showGatherRecordsDialog(gatherRecords, tempLink);
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

    private void getGatherTemperatureData(final JSONModel.TempLink tempLink, String cntuuid) {
        HashMap<String, String> map = getDefaultMap();
        if (cntuuid != null) {
            map.put("cntuuid", cntuuid);
        }
        map.put("linkuuid", tempLink.getLinkuuid());
        ConnectionUtil.postParams(Constants.HOST + Constants.GET_GATHER_TEMPERATURE_DATA_URL, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
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
                    return;
                }
                JSONModel.TempData[] tempDatas = gson.fromJson(returnObject.getM_ReturnOBJJsonArray(), JSONModel.TempData[].class);
                TagInfo tagInfo = new TagInfo();
                tagInfo.setUid(tempLink.getRfid());
                tagInfo.setLinkuuid(tempLink.getLinkuuid());
                tagInfo.setObject(tempLink.getCarno());
                tagInfo.setHavepost(true);

                JSONModel.Box box = new JSONModel.Box();
                box.setBoxno(tempLink.getBoxno());
                box.setBoxtype(tempLink.getBoxtype());
                tagInfo.setBox(gson.toJson(box));

                GoodsType goods = new GoodsType();
                goods.setGoodtype(tempLink.getGoodchildtype());
                goods.setParentgoodtype(tempLink.getGoodtype());
                goods.setOnetime(tempLink.getOnetime());

                JSONModel.TempData tempData = tempDatas[tempDatas.length - 1];
                goods.setLowhumiditynumber((int) tempData.getLowhumiditynumber());
                goods.setHighhumiditynumber((int) tempData.getHighhumiditynumber());
                goods.setLowtmpnumber((int) tempData.getLowtmpnumber());
                goods.setHightmpnumber((int) tempData.getHightmpnumber());
                tagInfo.setGoods(gson.toJson(goods));
                tagInfo.setHumMin(tempData.getHumiditynumber());
                tagInfo.setHumMax(tempData.getHumiditynumber());
                tagInfo.setTempMin(tempData.getTmpnumber());
                tagInfo.setTempMax(tempData.getTmpnumber());
                if (tempData.getHumiditynumber() == -99) {
                    tagInfo.setJustTemp(true);
                }

                ArrayList<Double> hum_list = new ArrayList<>();
                ArrayList<Double> temp_list = new ArrayList<>();
                ArrayList<String> timeArray = new ArrayList<>();
                for (int i = 0; i < tempDatas.length; i++) {
                    JSONModel.TempData tempData1 = tempDatas[i];
                    hum_list.add(tempData1.getHumiditynumber());
                    temp_list.add(tempData1.getTmpnumber());
                    timeArray.add(tempData1.getRecordtime().replaceAll("/", "-"));

                    tagInfo.setTempMin(Math.min(tempData1.getTmpnumber(), tagInfo.getTempMin()));
                    tagInfo.setTempMax(Math.max(tempData1.getTmpnumber(), tagInfo.getTempMax()));
                    tagInfo.setHumMin(Math.min(tempData1.getHumiditynumber(), tagInfo.getHumMin()));
                    tagInfo.setHumMax(Math.max(tempData1.getHumiditynumber(), tagInfo.getHumMax()));
                }

                tagInfo.setDataarray(gson.toJson(temp_list));
                tagInfo.setHumidityArray(gson.toJson(hum_list));

                Intent intent = new Intent(mContext, ChartActivity.class);
                intent.putExtra(Constants.TAG_INFO, tagInfo);
                intent.putExtra(Constants.IS_ON_LINE, true);
                intent.putExtra(Constants.TIME_ARRAY, timeArray);
                startActivity(intent);
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

    private void showGatherRecordsDialog(final JSONModel.GatherRecord[] gatherRecords, final JSONModel.TempLink tempLink) {
        if (gatherRecordsDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String[] items = new String[gatherRecords.length];
            for (int i = 0; i < gatherRecords.length; i++) {
                items[i] = gatherRecords[i].getCreatetime();
            }
            builder.setTitle("采集记录选择查看");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getGatherTemperatureData(tempLink, gatherRecords[which].getCntuuid());
                }
            });
            gatherRecordsDialog = builder.create();
        }
        gatherRecordsDialog.show();
    }

    @OnClick({R.id.image_year_pre, R.id.image_year_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_year_pre:
                year--;
                break;
            case R.id.image_year_next:
                year++;
                break;
        }
        textYear.setText(String.valueOf(year));
        searchLinks();
    }

    private class LinkAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.layout_temp_link_item, parent, false);
            LinkViewHolder holder = new LinkViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            LinkViewHolder viewHolder = (LinkViewHolder) holder;
            final JSONModel.TempLink tempLink = tempLinks[position];
            if (tempLink.isBover()) {
                viewHolder.text_endtime.setText(Utils.formatDateTime(tempLink.getEndtime()));
                viewHolder.text_end_address.setText(tempLink.getEndaddr());
                viewHolder.row_end_address.setVisibility(View.VISIBLE);
                viewHolder.row_endtime.setVisibility(View.VISIBLE);
            } else {
                viewHolder.row_end_address.setVisibility(View.GONE);
                viewHolder.row_endtime.setVisibility(View.GONE);
            }
            if (tempLink.getNumber() == 0) {
                viewHolder.btn_all.setVisibility(View.GONE);
                viewHolder.btn_detail.setVisibility(View.GONE);
            } else {
                viewHolder.btn_all.setVisibility(View.VISIBLE);
                viewHolder.btn_detail.setVisibility(View.VISIBLE);
            }
            viewHolder.text_object.setText(tempLink.getCarno());
            viewHolder.text_times.setText(String.valueOf(tempLink.getNumber()));
            viewHolder.text_starttime.setText(Utils.formatDateTime(tempLink.getBegintime()));
            viewHolder.text_start_address.setText(tempLink.getBeginaddr());
            viewHolder.text_goodstype.setText(tempLink.getGoodtype() + "-" + tempLink.getGoodchildtype());
            viewHolder.text_acter.setText(tempLink.getActrealname());

            viewHolder.btn_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getGatherRecords(tempLink);
                }
            });

            viewHolder.btn_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getGatherTemperatureData(tempLink, null);
                }
            });
        }

        @Override
        public int getItemCount() {
            return tempLinks.length;
        }
    }

    private class LinkViewHolder extends RecyclerView.ViewHolder {
        private TextView text_object;
        private TextView text_goodstype;
        private TextView text_acter;
        private TextView text_starttime;
        private TextView text_start_address;
        private TableRow row_endtime;
        private TextView text_endtime;
        private TableRow row_end_address;
        private TextView text_end_address;
        private TextView text_times;
        private Button btn_all;
        private Button btn_detail;

        public LinkViewHolder(View itemView) {
            super(itemView);

            text_acter = (TextView) itemView.findViewById(R.id.text_acter);
            text_end_address = (TextView) itemView.findViewById(R.id.text_end_address);
            text_endtime = (TextView) itemView.findViewById(R.id.text_end_time);
            text_goodstype = (TextView) itemView.findViewById(R.id.text_goodstype);
            text_object = (TextView) itemView.findViewById(R.id.text_object);
            text_start_address = (TextView) itemView.findViewById(R.id.text_start_address);
            text_starttime = (TextView) itemView.findViewById(R.id.text_starttime);
            text_times = (TextView) itemView.findViewById(R.id.text_times);
            row_endtime = (TableRow) itemView.findViewById(R.id.row_end_time);
            row_end_address = (TableRow) itemView.findViewById(R.id.row_end_address);
            btn_all = (Button) itemView.findViewById(R.id.btn_all);
            btn_detail = (Button) itemView.findViewById(R.id.btn_detail);
        }
    }
}
