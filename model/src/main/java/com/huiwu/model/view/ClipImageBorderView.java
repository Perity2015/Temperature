package com.huiwu.model.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class ClipImageBorderView extends View {
	private int mVerticalPadding;
	private int mWidth;
	private int mBorderColor;
	private int mBorderWidth;
	private Paint mPaint;

	public ClipImageBorderView(Context context) {
		this(context, (AttributeSet)null);
	}

	public ClipImageBorderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClipImageBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mBorderColor = Color.parseColor("#FFFFFF");
		this.mBorderWidth = 1;
		this.mBorderWidth = (int) TypedValue.applyDimension(1, (float) this.mBorderWidth, this.getResources().getDisplayMetrics());
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.mWidth = this.getWidth();
		this.mVerticalPadding = (this.getHeight() - this.mWidth) / 2;
		this.mPaint.setColor(Color.parseColor("#aa000000"));
		this.mPaint.setStyle(Paint.Style.FILL);
		canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.mVerticalPadding, this.mPaint);
		canvas.drawRect(0.0F, (float)(this.getHeight() - this.mVerticalPadding), (float)this.getWidth(), (float)this.getHeight(), this.mPaint);
		this.mPaint.setColor(this.mBorderColor);
		this.mPaint.setStrokeWidth((float)this.mBorderWidth);
		this.mPaint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(0.0F, (float)this.mVerticalPadding, (float)this.getWidth(), (float)(this.getHeight() - this.mVerticalPadding), this.mPaint);
	}
}
