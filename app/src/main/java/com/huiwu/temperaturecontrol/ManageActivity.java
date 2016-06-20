package com.huiwu.temperaturecontrol;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.huiwu.temperaturecontrol.fragment.utils.FragmentUtils;

public class ManageActivity extends BaseActivity {
    public static final String BOX_EXTRA = "BOX_EXTRA";
    public static final String OPTION_EXTRA = "OPTION_EXTRA";
    public static final String TMP_LINK_EXTRA = "TMP_LINK_EXTRA";

    public static final String OPTION_NEW_BOX = "newBox";
    public static final String OPTION_CONFIG = "config";
    public static final String OPTION_UNBIND = "unBind";
    public static final String OPTION_SEAL = "seal";
    public static final String OPTION_OPENED = "opened";

    public JSONModel.Box box;
    public JSONModel.TempLink tempLink;
    public String option;

    private FragmentManager fragmentManager = null;
    private FragmentUtils fragmentUtils;
    private String currentFlag;

    private String[] manageOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        manageOptions = getResources().getStringArray(R.array.manageOptions);

        fragmentManager = getSupportFragmentManager();
        fragmentUtils = FragmentUtils.getInstance(fragmentManager);

        box = getIntent().getParcelableExtra(BOX_EXTRA);
        option = getIntent().getStringExtra(OPTION_EXTRA);
        tempLink = getIntent().getParcelableExtra(TMP_LINK_EXTRA);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);


        setSelectFragment(option);
    }

    public void setSelectFragment(String tagFlag) {
        int position = 0;
        switch (tagFlag) {
            case OPTION_NEW_BOX:
                position = 0;
                break;
            case OPTION_CONFIG:
                position = 1;
                break;
            case OPTION_SEAL:
                position = 2;
                break;
            case OPTION_OPENED:
                position = 3;
                break;
            case OPTION_UNBIND:
                position = 4;
                break;
        }
        TextView text_title = (TextView) findViewById(R.id.text_manage_title);
        text_title.setText(manageOptions[position]);
        if (TextUtils.equals(currentFlag, tagFlag))
            return;
        if (!TextUtils.isEmpty(currentFlag)) {
            fragmentUtils.detachFragment(fragmentUtils.getFragment(currentFlag));
        }
        boolean flag = fragmentUtils.attachFragment(R.id.fragment_container, fragmentUtils.getFragment(tagFlag), tagFlag);
        if (flag) {
            currentFlag = tagFlag;
        }
    }

}
