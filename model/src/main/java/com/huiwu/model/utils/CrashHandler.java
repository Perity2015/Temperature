package com.huiwu.model.utils;

import android.content.Context;
import android.os.Process;
import android.text.format.DateFormat;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private static CrashHandler instance;
	private static final String TAG = "Exception";

	private CrashHandler(Context context) {
	}

	public static synchronized CrashHandler getInstance(Context context) {
		if(instance == null) {
			instance = new CrashHandler(context);
		}

		return instance;
	}

	public void init(Context context) {
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		String errorMsg = DateFormat.format("yyyy-MM-dd kk:mm:ss", System.currentTimeMillis()).toString() + "\n";
		errorMsg = errorMsg + "uncaughtException, thread: " + thread.toString() + "\n";
		errorMsg = errorMsg + " name: " + thread.getName() + "\n";
		errorMsg = errorMsg + " id: " + thread.getId() + "\n";
		errorMsg = errorMsg + "exception: " + ex + "\n\n";
		Utils.saveRecordToFile(TAG, errorMsg);
		Process.killProcess(Process.myPid());
		System.exit(0);
	}
}
