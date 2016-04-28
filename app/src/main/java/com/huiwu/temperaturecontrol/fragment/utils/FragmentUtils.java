package com.huiwu.temperaturecontrol.fragment.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.huiwu.temperaturecontrol.ChooseActivity;
import com.huiwu.temperaturecontrol.ManageActivity;
import com.huiwu.temperaturecontrol.fragment.ChooseGoodsFragment;
import com.huiwu.temperaturecontrol.fragment.ChooseObjectFragment;
import com.huiwu.temperaturecontrol.fragment.ConfigFragment;
import com.huiwu.temperaturecontrol.fragment.NewBoxFragment;
import com.huiwu.temperaturecontrol.fragment.OpenFragment;
import com.huiwu.temperaturecontrol.fragment.SealFragment;

public class FragmentUtils {
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    public FragmentUtils(FragmentManager fragmentManager) {
        super();
        this.fragmentManager = fragmentManager;
    }


    public static FragmentUtils getInstance(FragmentManager fragmentManager) {
        return new FragmentUtils(fragmentManager);
    }


    public FragmentTransaction ensureTransaction() {
        if (fragmentTransaction == null) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        return fragmentTransaction;
    }

    public boolean attachFragment(int layout, Fragment fragment, String tag) {
        if (fragment != null) {
            if (fragment.isDetached()) {
                ensureTransaction();
                fragmentTransaction.attach(fragment);
            } else if (!fragment.isAdded()) {
                ensureTransaction();
                fragmentTransaction.add(layout, fragment, tag);
            }
            if (fragmentTransaction != null && !fragmentTransaction.isEmpty()) {
                fragmentTransaction.commit();
                fragmentTransaction = null;
                return true;
            }
        }
        return false;
    }

    public Fragment getFragment(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case ManageActivity.OPTION_CONFIG:
                    fragment = new ConfigFragment();
                    break;
                case ManageActivity.OPTION_NEW_BOX:
                    fragment = new NewBoxFragment();
                    break;
                case ManageActivity.OPTION_OPENED:
                    fragment = new OpenFragment();
                    break;
                case ManageActivity.OPTION_UNBIND:
                    break;
                case ManageActivity.OPTION_SEAL:
                    fragment = new SealFragment();
                    break;
                case ChooseActivity.CHOOSE_GOODS:
                    fragment = new ChooseGoodsFragment();
                    break;
                case ChooseActivity.CHOOSE_OBJECT:
                    fragment = new ChooseObjectFragment();
                    break;
            }
        }
        return fragment;
    }

    public void detachFragment(Fragment fragment) {
        if (fragment != null && !fragment.isDetached()) {
            ensureTransaction();
            fragmentTransaction.detach(fragment);
        }
    }

}
