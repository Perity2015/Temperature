package com.huiwu.temperaturecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huiwu.temperaturecontrol.bean.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {
    @Bind(R.id.viewPager)
    ViewPager viewPager;

    private Handler mHandler = new Handler();

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        findViewById(R.id.layout_start).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getSharedPreferences(Constants.SHARED, MODE_PRIVATE).contains(Constants.VERSION)) {
                    startActivity(new Intent(StartActivity.this, LoginActivity.class));
                    finish();
                } else {
                    getSharedPreferences(Constants.SHARED, MODE_PRIVATE).edit().putBoolean(Constants.VERSION, true).commit();
                    viewPager.setVisibility(View.VISIBLE);
                }
            }
        }, 2000);
    }

    public static class PlaceholderFragment extends Fragment {
        private int[] imageIds = {R.drawable.image_guide_1, R.drawable.image_guide_2, R.drawable.image_guide_3};

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_start, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.section_image);
            int position = getArguments().getInt(ARG_SECTION_NUMBER);
            imageView.setImageResource(imageIds[position]);
            if (position == 2) {
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                        return true;
                    }
                });
            } else {
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
            }
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

}
