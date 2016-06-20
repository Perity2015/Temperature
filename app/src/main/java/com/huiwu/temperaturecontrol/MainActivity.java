package com.huiwu.temperaturecontrol;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.huiwu.temperaturecontrol.fragment.GatherFragment;
import com.huiwu.temperaturecontrol.fragment.HomeFragment;
import com.huiwu.temperaturecontrol.fragment.RecordFragment;
import com.huiwu.temperaturecontrol.fragment.UserFragment;

public class MainActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will HOST the section contents.
     */
    private ViewPager mViewPager;

    private RadioGroup radioGroup;

    private TextView textMainTitle;

    private Fragment[] fragments;

    private String[] titles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textMainTitle = (TextView) findViewById(R.id.text_main_title);

        titles = getResources().getStringArray(R.array.mTitles);

        fragments = new Fragment[4];
        fragments[0] = new HomeFragment();
        fragments[1] = new GatherFragment();
        fragments[2] = new RecordFragment();
        fragments[3] = new UserFragment();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(fragments.length);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_home:
                        mViewPager.setCurrentItem(0, false);
                        break;
                    case R.id.radio_gather:
                        mViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.radio_records:
                        mViewPager.setCurrentItem(2, false);
                        break;
                    case R.id.radio_mine:
                        mViewPager.setCurrentItem(3, false);
                        break;
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                textMainTitle.setText(titles[position]);
                switch (position) {
                    case 0:
                        textMainTitle.setPadding(0, 0, 0, 0);
                        radioGroup.check(R.id.radio_home);
                        break;
                    case 1:
                        textMainTitle.setPadding((int) getResources().getDimension(android.R.dimen.app_icon_size), 0, 0, 0);
                        radioGroup.check(R.id.radio_gather);
                        break;
                    case 2:
                        textMainTitle.setPadding((int) getResources().getDimension(android.R.dimen.app_icon_size), 0, 0, 0);
                        radioGroup.check(R.id.radio_records);
                        break;
                    case 3:
                        textMainTitle.setPadding(0, 0, 0, 0);
                        radioGroup.check(R.id.radio_mine);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        radioGroup.check(R.id.radio_home);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments[position];
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
