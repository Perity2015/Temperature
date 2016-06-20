package com.huiwu.temperaturecontrol.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.JSONModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManageFragment extends BaseFragment {
    public ManageActivity manageActivity;
    public JSONModel.Box box;
    public JSONModel.TempLink tempLink;
    public String option;

    public ManageFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manageActivity = (ManageActivity) getActivity();
        box = manageActivity.box;
        tempLink = manageActivity.tempLink;
        option = manageActivity.option;
    }
}
