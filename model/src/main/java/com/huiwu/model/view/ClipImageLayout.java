package com.huiwu.model.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class ClipImageLayout extends RelativeLayout {
	private ZoomImageView mZoomImageView;
	private ClipImageBorderView mClipImageView;

	public ClipImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mZoomImageView = new ZoomImageView(context);
		this.mClipImageView = new ClipImageBorderView(context);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(this.mZoomImageView, lp);
		this.addView(this.mClipImageView, lp);
	}

	public ZoomImageView getmZoomImageView() {
		return mZoomImageView;
	}

	public void setImage(Bitmap bitmap) {
		this.mZoomImageView.setImageBitmap(bitmap);
	}

	public Bitmap clip() {
		return this.mZoomImageView.clip();
	}
}

