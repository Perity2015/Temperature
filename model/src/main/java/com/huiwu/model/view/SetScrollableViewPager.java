package com.huiwu.model.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yc on 2015/12/2.
 */
public class SetScrollableViewPager extends ViewPager {

    private boolean isCanScroll = true;

    public SetScrollableViewPager(Context context) {
        super(context);
    }

    public SetScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isCanScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }
}