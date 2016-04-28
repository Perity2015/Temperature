package com.huiwu.temperaturecontrol;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;
import com.huiwu.model.utils.Utils;
import com.huiwu.model.view.utils.ScreenUtils;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChartActivity extends BaseActivity {

    @Bind(R.id.text_info_object)
    TextView textInfoObject;
    @Bind(R.id.text_box_no)
    TextView textBoxNo;
    @Bind(R.id.text_sample_interval)
    TextView textSampleInterval;
    @Bind(R.id.text_temp_waring)
    TextView textTempWaring;
    @Bind(R.id.text_hum_waring)
    TextView textHumWaring;
    @Bind(R.id.row_hum_waring)
    TableRow rowHumWaring;
    @Bind(R.id.text_start_day)
    TextView textStartDay;
    @Bind(R.id.text_start_time)
    TextView textStartTime;
    @Bind(R.id.text_record_now)
    TextView textRecordNow;
    @Bind(R.id.text_temp_size)
    TextView textTempSize;
    @Bind(R.id.text_hum_size)
    TextView textHumSize;
    @Bind(R.id.row_hum_size)
    TableRow rowHumSize;
    @Bind(R.id.text_time_record_start_day)
    TextView textTimeRecordStartDay;
    @Bind(R.id.text_time_record_start_time)
    TextView textTimeRecordStartTime;
    @Bind(R.id.text_time_record_year)
    TextView textTimeRecordYear;
    @Bind(R.id.text_time_record_end_day)
    TextView textTimeRecordEndDay;
    @Bind(R.id.text_time_record_end_time)
    TextView textTimeRecordEndTime;
    @Bind(R.id.lineChart)
    LineChart lineChart;
    @Bind(R.id.layout_info)
    LinearLayout layoutInfo;
    @Bind(R.id.layout_chart)
    LinearLayout layoutChart;
    @Bind(R.id.action_up)
    FloatingActionButton actionUp;

    private JSONModel.TagInfo tagInfo;

    private LineData data;
    private int screen_width;
    private int screen_height;

    private boolean haveUpload;
    private boolean isAnimation;
    private ArrayList<String> timeArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_back);

        screen_width = ScreenUtils.getScreenWidth(mContext);
        screen_height = ScreenUtils.getScreenHeight(mContext);

        MyMarkerView mv = new MyMarkerView(mContext, R.layout.custom_marker_view);
        lineChart.setMarkerView(mv);

        tagInfo = getIntent().getParcelableExtra(Constants.tag_info);

        haveUpload = tagInfo.isHavepost();
        if (getIntent().getBooleanExtra(Constants.is_on_line, false)) {
            timeArray = getIntent().getStringArrayListExtra("timeArray");
        } else {
            timeArray = new ArrayList<>();
            for (int i = 0; i < tagInfo.getTempList().size(); i++) {
                String time = DateFormat.format("yyyy-MM-dd kk:mm:ss", tagInfo.getStartTime() + tagInfo.getGoods().getOnetime() * 60 * 1000L * i).toString();
                timeArray.add(time);
            }
        }

        initView();

        setupChart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("haveUpload", haveUpload);
        outState.putParcelable(Constants.tag_info, tagInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        haveUpload = savedInstanceState.getBoolean("haveUpload");
        tagInfo = savedInstanceState.getParcelable(Constants.tag_info);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tagInfo.getBox() == null || tagInfo.getObject() == null)
            getTempLinkInfo(tagInfo.getLinkuuid());
    }

    private void getTempLinkInfo(String linkuuid) {
        cancelConnectionTask();
        HashMap<String, String> map = getDefaultMap();
        map.put("linkuuid", linkuuid);
        task = new ConnectionTask(map, new ConnectionHandler() {
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
                JSONModel.ReturnData returnData = gson.fromJson(result, JSONModel.ReturnData.class);
                JSONModel.TempLink tempLink = gson.fromJson(returnData.getRows().get(0), JSONModel.TempLink.class);
                textInfoObject.setText(tempLink.getCarno());
                textBoxNo.setText(tempLink.getBoxno());

                tagInfo.setObject(tempLink.getCarno());

                JSONModel.Box box = new JSONModel.Box();
                box.setBoxtype(tempLink.getBoxtype());
                box.setBoxno(tempLink.getBoxno());
                tagInfo.setBox(box);

                JSONModel.Goods goods;
                if (tagInfo.getGoods() != null) {
                    goods = tagInfo.getGoods();
                } else {
                    goods = new JSONModel.Goods();
                }
                goods.setParentgoodtype(tempLink.getGoodtype());
                goods.setGoodtype(tempLink.getGoodchildtype());
                tagInfo.setGoods(goods);

                sqLiteManage.updateRecords(tagInfo);
            }

            @Override
            public void sendLost(String result) {

            }
        });
        task.execute(Constants.get_temperature_links_url);
    }

    private void initView() {
        if (tagInfo.isJustTemp()) {
            rowHumSize.setVisibility(View.GONE);
            rowHumWaring.setVisibility(View.GONE);
            textRecordNow.setText(tagInfo.getTem_now() + "℃");
        } else {
            textRecordNow.setText(tagInfo.getTem_now() + "℃、" + tagInfo.getHum_now() + "%");
        }

        if (tagInfo.getObject() != null) {
            textInfoObject.setText(tagInfo.getObject());
        }
        if (tagInfo.getBox() != null) {
            textBoxNo.setText(tagInfo.getBox().getBoxno());
        }


        JSONModel.Goods goods = tagInfo.getGoods();
        textSampleInterval.setText(goods.getOnetime() + "分钟");
        textTempWaring.setText(goods.getLowtmpnumber() + "℃、" + goods.getHightmpnumber() + "℃");
        textHumWaring.setText(goods.getLowhumiditynumber() + "%、" + goods.getHighhumiditynumber() + "%");

        String timeStart = timeArray.get(0);
        String timeEnd = timeArray.get(timeArray.size() - 1);

        textStartDay.setText(timeArray.get(0).split(" ")[0]);
        textStartTime.setText(timeArray.get(0).split(" ")[1]);
        textTimeRecordStartTime.setText(timeArray.get(0).split(" ")[1]);
        textTimeRecordEndTime.setText(timeArray.get(timeArray.size() - 1).split(" ")[1]);
        String[] start = timeArray.get(0).split(" ")[0].split("-");
        textTimeRecordStartDay.setText(start[1] + "-" + start[2]);
        String[] end = timeArray.get(timeArray.size() - 1).split(" ")[0].split("-");
        textTimeRecordEndDay.setText(end[1] + "-" + end[2]);
        textTimeRecordYear.setText(end[0]);

        textTempSize.setText(tagInfo.getTemp_min() + "℃ - " + tagInfo.getTemp_max() + "℃");
        textHumSize.setText(tagInfo.getHum_min() + "% - " + tagInfo.getHum_max() + "%");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        return true;
    }

    private LineData getData() {
        ArrayList<LineDataSet> dataSets = new ArrayList<>();

        ArrayList<String> xVals = timeArray;
        ArrayList<Entry> yVals = new ArrayList<>();
        for (int i = 0; i < tagInfo.getTempList().size(); i++) {
            double val = tagInfo.getTempList().get(i);
            yVals.add(new Entry((float) val, i));
        }
        LineDataSet set1 = new LineDataSet(yVals, "温度℃");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setDrawValues(false);
        set1.setDrawCubic(true);
        set1.setLineWidth(2f);
        set1.setDrawCircles(false);
        set1.setColor(Color.parseColor("#EBD7A3"));
        set1.setHighLightColor(Color.BLACK);

        dataSets.add(set1);

        if (!tagInfo.isJustTemp()) {
            yVals = new ArrayList<>();
            for (int i = 0; i < tagInfo.getHumList().size(); i++) {
                double val = tagInfo.getHumList().get(i);
                yVals.add(new Entry((float) val, i));
            }
            LineDataSet set2 = new LineDataSet(yVals, "湿度%");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setDrawCubic(true);
            set2.setDrawValues(false);
            set2.setLineWidth(2f);
            set2.setDrawCircles(false);
            set2.setColor(Color.parseColor("#5BA7E2"));
            set2.setHighLightColor(Color.BLACK);

            dataSets.add(set2);
        }


        LineData data = new LineData(xVals, dataSets);
        return data;
    }

    private void setupChart() {
        data = getData();

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#EBD7A3"));
        leftAxis.setTextSize(12);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawTopYLabelEntry(true);
        leftAxis.setStartAtZero(false);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return String.format("%.1f℃", value);
            }
        });
        leftAxis.setAxisMaxValue((float) (tagInfo.getTemp_max() + 5));
        leftAxis.setAxisMinValue((float) (tagInfo.getTemp_min() - 5));

        if (!tagInfo.isJustTemp()) {
            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setTextColor(Color.parseColor("#5BA7E2"));
            rightAxis.setTextSize(12);
            rightAxis.setStartAtZero(false);
            rightAxis.setDrawGridLines(false);
            rightAxis.setDrawTopYLabelEntry(true);
            rightAxis.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, YAxis yAxis) {
                    return String.format("%.1f", value) + "%";
                }
            });
            rightAxis.setAxisMaxValue((float) (tagInfo.getHum_max() + 5));
            rightAxis.setAxisMinValue((float) (tagInfo.getHum_min() - 5));
        } else {
            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setTextColor(Color.parseColor("#ECEFF4"));
            rightAxis.setDrawGridLines(false);
        }


        Legend l = lineChart.getLegend();
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setFormSize(6f);
        l.setTextColor(Color.BLACK);

        lineChart.setDescription("");
        lineChart.setBackgroundColor(Color.parseColor("#ECEFF4"));
        lineChart.setDescriptionTextSize(10);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setData(data);
        lineChart.getXAxis().setTextColor(Color.TRANSPARENT);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.invalidate();
        lineChart.animateX(2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_upload) {
            if (haveUpload) {
                return true;
            }
            uploadData();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.action_up)
    public void onClick() {
        if (isAnimation) {
            return;
        }
        Animation animation_circle = AnimationUtils.loadAnimation(mContext, R.anim.anim_action_circle);
        animation_circle.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        actionUp.startAnimation(animation_circle);

        View view_from_left = null;
        View view_to_right = null;
        if (layoutChart.getVisibility() == View.VISIBLE) {
            view_to_right = layoutChart;
            view_from_left = layoutInfo;
        } else {
            view_to_right = layoutInfo;
            view_from_left = layoutChart;
        }
        Animation animation_from_left = AnimationUtils.loadAnimation(mContext, R.anim.anim_from_left);
        final View finalView_from_left = view_from_left;
        animation_from_left.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                finalView_from_left.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view_from_left.startAnimation(animation_from_left);
        Animation animation_to_roght = AnimationUtils.loadAnimation(mContext, R.anim.anim_to_right);
        final View finalView_to_right = view_to_right;
        animation_to_roght.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finalView_to_right.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view_to_right.startAnimation(animation_to_roght);
    }

    public class MyMarkerView extends MarkerView {
        private TextView tvContent;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int xIndex = e.getXIndex();
            float temp = data.getDataSetByIndex(0).getYVals().get(xIndex).getVal();
            if (tagInfo.isJustTemp()) {
                tvContent.setText(data.getXVals().get(e.getXIndex()) + "\n温度：" + temp + "℃");
                return;
            }
            float hum = data.getDataSetByIndex(1).getYVals().get(xIndex).getVal();
            tvContent.setText(data.getXVals().get(e.getXIndex()) + "\n温度：" + temp + "℃" + "\n湿度：" + hum + "%");
        }

        @Override
        public int getXOffset(float xpos) {
            if (xpos > screen_width / 2) {
                return -tvContent.getWidth();
            }
            return 0;
        }

        @Override
        public int getYOffset(float ypos) {
            if (ypos > screen_height / 2) {
                return -tvContent.getHeight();
            }
            return 0;
        }

    }

    private void uploadData() {
        cancelConnectionTask();
        HashMap<String, String> map = getDefaultMap();
        if (mainApp.bdLocation != null) {
            map.put("address", mainApp.bdLocation.getAddrStr());
        } else {
            map.put("address", "为获取定位信息");
        }
        map.put("dataarray", gson.toJson(tagInfo.getTempList()));
        if (!tagInfo.isJustTemp()) {
            map.put("humidityArray", gson.toJson(tagInfo.getHumList()));
        }
        map.put("m_begintime", timeArray.get(0));
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("rfid", tagInfo.getUid());
        map.put("bover", String.valueOf(false));
        map.put("roundCircle", String.valueOf(tagInfo.getRoundCircle()));
        map.put("index", String.valueOf(tagInfo.getIndex()));

        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage("上传记录信息中……");
                progressDialog.show();
            }

            @Override
            public void sendFinish() {
                progressDialog.hide();
            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    uploadOfflineData();
                    return;
                }
                haveUpload = true;
                sqLiteManage.changeRecordStatus(tagInfo);
                showNoticeDialog(returnObject.getsMsg(), false);
            }

            @Override
            public void sendLost(String result) {
                loginAgain();
            }
        });
        task.execute(Constants.upload_data_url);
    }

    private void uploadOfflineData() {
        cancelConnectionTask();
        HashMap<String, String> map = getDefaultMap();
        if (mainApp.bdLocation != null) {
            map.put("address", mainApp.bdLocation.getAddrStr());
        } else {
            map.put("address", "为获取定位信息");
        }
        map.put("dataarray", gson.toJson(tagInfo.getTempList()));
        if (!tagInfo.isJustTemp()) {
            map.put("humidityArray", gson.toJson(tagInfo.getHumList()));
        }
        map.put("rfid", tagInfo.getUid());
        map.put("bover", String.valueOf(false));
        map.put("begintime", timeArray.get(0));
        map.put("roundCircle", String.valueOf(tagInfo.getRoundCircle()));
        map.put("index", String.valueOf(tagInfo.getIndex()));
        map.put("linkuuid", tagInfo.getLinkuuid());
        map.put("createtime", Utils.formatDateTimeOffLine(tagInfo.getReadTime()));


        task = new ConnectionTask(map, new ConnectionHandler() {
            @Override
            public void sendStart() {
                progressDialog.setMessage("上传记录信息中……");
                progressDialog.show();
            }

            @Override
            public void sendFinish() {
                progressDialog.hide();
            }

            @Override
            public void sendFailed(String result) {

            }

            @Override
            public void sendSuccess(String result) {
                JSONModel.ReturnObject returnObject = gson.fromJson(result, JSONModel.ReturnObject.class);
                if (!returnObject.isbOK()) {
                    Utils.showLongToast(returnObject.getsMsg(), mContext);
                    return;
                }
                haveUpload = true;
                sqLiteManage.changeRecordStatus(tagInfo);
                showNoticeDialog(returnObject.getsMsg(), false);
            }

            @Override
            public void sendLost(String result) {
                loginAgain();
            }
        });
        task.execute(Constants.upload_data_offline_url);
    }

}
