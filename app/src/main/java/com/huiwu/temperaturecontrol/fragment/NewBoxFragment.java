package com.huiwu.temperaturecontrol.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.huiwu.model.http.ConnectionUtil;
import com.huiwu.model.http.StringConnectionCallBack;
import com.huiwu.model.utils.Utils;
import com.huiwu.model.view.MyAlertDialog;
import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.R;
import com.huiwu.temperaturecontrol.bean.Constants;
import com.huiwu.temperaturecontrol.bean.JSONModel;
import com.lzy.okhttputils.request.BaseRequest;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewBoxFragment extends ManageFragment {


    @Bind(R.id.text_box_no)
    TextView textBoxNo;
    @Bind(R.id.text_box_describe)
    EditText textBoxDescribe;
    @Bind(R.id.spinner_seal_status)
    Spinner spinnerSealStatus;

    private String[] sealStatus = {"", "normal", "seal", "LOCK"};

    public NewBoxFragment() {
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
        View view = inflater.inflate(R.layout.fragment_new_box, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textBoxNo.setText(manageActivity.box.getBoxno());
        spinnerSealStatus.setAdapter(new ArrayAdapter<>(getContext(), R.layout.layout_seal_status_item, getResources().getStringArray(R.array.sealStatus)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.confirm);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                baseActivity.hideSoftInput();
                String text_describe = textBoxDescribe.getText().toString().trim();
                if (TextUtils.isEmpty(text_describe) || spinnerSealStatus.getSelectedItemPosition() == 0) {
                    Utils.showLongToast(R.string.incomplete_information, getContext());
                    return true;
                }
                addNewBox(text_describe);
                return true;
            }
        });
    }

    private void addNewBox(final String describe) {
        HashMap<String, String> map = baseActivity.getDefaultMap();
        map.put("boxno", manageActivity.box.getBoxno());
        map.put("boxmemo", describe);
        map.put("companyid", String.valueOf(userInfo.getMessage().getCompanyid()));
        map.put("orgna_id", String.valueOf(userInfo.getMessage().getOrgna_id()));
        map.put("boxtype", sealStatus[spinnerSealStatus.getSelectedItemPosition()]);
        ConnectionUtil.postParams(Constants.HOST + Constants.ADD_BOX, map, new StringConnectionCallBack() {
            @Override
            public void sendStart(BaseRequest baseRequest) {
                progressDialog.setMessage(getString(R.string.submit_load));
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
                    Utils.showLongToast(returnObject.getsMsg(), getContext());
                    return;
                }
                manageActivity.box = gson.fromJson(returnObject.getM_ReturnOBJJsonObject().get("box"), JSONModel.Box.class);
                showFinishAddDialog();
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

    private void showFinishAddDialog() {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.do_success_to_connitue));
        builder.setPositiveButton(getString(R.string.next_step), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                manageActivity.option = ManageActivity.OPTION_CONFIG;
                manageActivity.setSelectFragment(manageActivity.option);
            }
        });
        builder.setNegativeButton(getString(R.string.over_step), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                manageActivity.finish();
            }
        });
        builder.show();
    }
}
