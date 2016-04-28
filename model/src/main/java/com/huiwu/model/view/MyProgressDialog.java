package com.huiwu.model.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.huiwu.model.R;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class MyProgressDialog extends Dialog {
	private static MyProgressDialog myProgressDialog = null;
	private static Context mContext;

	public MyProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public static MyProgressDialog createDialog(Context context) {
		mContext = context;
		myProgressDialog = new MyProgressDialog(context, R.style.custom_dialog_style);
		myProgressDialog.setContentView(R.layout.layout_my_progress_dialog);
		myProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		return myProgressDialog;
	}

	public void onWindowFocusChanged(boolean hasFocus){

		if (myProgressDialog == null){
			return;
		}

		ImageView imageView = (ImageView) myProgressDialog.findViewById(R.id.image_loading);
		Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.progress_dialog_loading);
		imageView.startAnimation(animation);
	}

	public MyProgressDialog setMessage(String strMessage){
		TextView tvMsg = (TextView) myProgressDialog.findViewById(R.id.text_loading);

		if (tvMsg != null){
			tvMsg.setText(strMessage);
		}

		return myProgressDialog;
	}
}

