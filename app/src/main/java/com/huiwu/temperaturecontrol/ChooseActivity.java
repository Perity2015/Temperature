package com.huiwu.temperaturecontrol;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.huiwu.temperaturecontrol.fragment.utils.FragmentUtils;

public class ChooseActivity extends BaseActivity {
    public static final String CHOOSE_FLAG = "choose_flag";

    private FragmentManager fragmentManager = null;
    private FragmentUtils fragmentUtils;
    private String currentFlag;

    public static final String CHOOSE_GOODS = "choose_goods";
    public static final String CHOOSE_OBJECT = "choose_object";

    private String[] chooseTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        chooseTitles = getResources().getStringArray(R.array.chooseTitles);

        fragmentManager = getSupportFragmentManager();
        fragmentUtils = FragmentUtils.getInstance(fragmentManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_back);

        setSelectFragment(getIntent().getStringExtra(CHOOSE_FLAG));
    }

    public void setSelectFragment(String tagFlag) {
        int position = 0;
        switch (tagFlag) {
            case CHOOSE_GOODS:
                position = 0;
                break;
            case CHOOSE_OBJECT:
                position = 1;
                break;
        }
        TextView text_title = (TextView) findViewById(R.id.text_choose_title);
        text_title.setText(chooseTitles[position]);
        if (TextUtils.equals(currentFlag, tagFlag))
            return;
        if (!TextUtils.isEmpty(currentFlag)) {
            fragmentUtils.detachFragment(fragmentUtils.getFragment(currentFlag));
        }
        boolean flag = fragmentUtils.attachFragment(R.id.fragment_choose_container, fragmentUtils.getFragment(tagFlag), tagFlag);
        if (flag) {
            currentFlag = tagFlag;
        }
    }
}
