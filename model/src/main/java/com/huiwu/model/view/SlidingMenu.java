package com.huiwu.model.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.huiwu.model.view.utils.ScreenUtils;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class SlidingMenu extends HorizontalScrollView {
	private int mScreenWidth;
	private int mMenuWidth;
	private int mHalfMenuWidth;
	private boolean isOpen;
	private boolean isFirst;
	private ViewGroup mMenu;
	private ViewGroup mContent;
	private OpenListener listener;

	public boolean isOpen() {
		return this.isOpen;
	}

	public void setIsOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public boolean isFirst() {
		return this.isFirst;
	}

	public void setIsFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public SlidingMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mScreenWidth = ScreenUtils.getScreenWidth(context);
		this.mMenuWidth = this.mScreenWidth * 2 / 3;
		this.mHalfMenuWidth = this.mMenuWidth / 2;
	}

	public SlidingMenu(Context context) {
		this(context, null, 0);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(!this.isFirst) {
			LinearLayout wrapper = (LinearLayout)this.getChildAt(0);
			this.mMenu = (ViewGroup)wrapper.getChildAt(0);
			this.mContent = (ViewGroup)wrapper.getChildAt(1);
			this.mMenu.getLayoutParams().width = this.mMenuWidth;
			this.mContent.getLayoutParams().width = this.mScreenWidth;
			this.mContent.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isOpen()) {
						closeMenu();
					}
				}
			});
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(changed) {
			this.scrollTo(this.mMenuWidth, 0);
			this.isFirst = true;
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch(action) {
			case 1:
				int scrollX = this.getScrollX();
				if(scrollX < this.mHalfMenuWidth) {
					this.openMenu();
				} else {
					this.closeMenu();
				}
				return true;
			default:
				return super.onTouchEvent(event);
		}
	}

	public void openMenu() {
		this.smoothScrollTo(0, 0);
		this.isOpen = true;
		if (listener != null) {
			listener.open();
		}
	}

	public void closeMenu() {
		this.smoothScrollTo(this.mMenuWidth, 0);
		this.isOpen = false;
		if (listener != null) {
			listener.close();
		}
	}

	public void toggle() {
		if(this.isOpen) {
			this.closeMenu();
		} else {
			this.openMenu();
		}
	}

	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		super.onScrollChanged(l, t, oldl, oldt);
		float scale = (float)l * 1.0F / (float)this.mMenuWidth;
		float leftScale = 1.0F - 0.3F * scale;
		float rightScale = 0.8F + scale * 0.2F;
		ViewHelper.setScaleX(this.mMenu, leftScale);
		ViewHelper.setScaleY(this.mMenu, leftScale);
		ViewHelper.setAlpha(this.mMenu, 0.6F + 0.4F * (1.0F - scale));
		ViewHelper.setTranslationX(this.mMenu, (float)this.mMenuWidth * scale * 0.6F);
		ViewHelper.setPivotX(this.mContent, 0.0F);
		ViewHelper.setPivotY(this.mContent, (float)(this.mContent.getHeight() / 2));
		ViewHelper.setScaleX(this.mContent, rightScale);
		ViewHelper.setScaleY(this.mContent, rightScale);
	}

	public void setOpenListener(OpenListener listener) {
		this.listener = listener;
	}

	public interface OpenListener {
		void open();

		void close();
	}
}
