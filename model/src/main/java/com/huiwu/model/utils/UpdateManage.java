package com.huiwu.model.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.view.KeyEvent;

import com.huiwu.model.http.ConnectionHandler;
import com.huiwu.model.http.ConnectionTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class UpdateManage {
	private static String saveFileName;
	private Context mContext;
	private String apkVer;
	private ProgressDialog dialog;
	private boolean isZH;
	private boolean flag;
	private boolean need_toast;

	public UpdateManage(Context context,ProgressDialog dialog, boolean isZH,boolean need_toast) {
		this.mContext = context;
		this.isZH = isZH;
		this.need_toast = need_toast;
		this.dialog = dialog;
		this.dialog.setCancelable(false);
		this.dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					UpdateManage.this.flag = true;
					dialog.dismiss();
				}
				return false;
			}
		});

		try {
			PackageManager e = context.getPackageManager();
			PackageInfo packInfo = e.getPackageInfo(context.getPackageName(), 0);
			this.apkVer = packInfo.versionName;
		} catch (PackageManager.NameNotFoundException var5) {
			var5.printStackTrace();
		}

	}

	public void checkVersion(String appName) {
		HashMap map = new HashMap();
		map.put("appname", appName);
		map.put("ver", this.apkVer);
		ConnectionTask connectionTask = new ConnectionTask(map, new ConnectionHandler() {
			public void sendSuccess(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONObject e = jsonObject.getJSONObject("m_ReturnOBJ");
					if(e.getBoolean("NewVer")) {
						String apkUrl = "http://www.yunrfid.com" + e.getString("DownUrl");
						String verName = e.getString("ver");
						String updateMsg = UpdateManage.this.isZH?"有最新的软件包哦，请点击下载\n最新版本：" + verName:"New Version:" + verName;
						UpdateManage.this.showNoticeDialog(apkUrl, updateMsg);
					}else {
						if (need_toast){
							Utils.showLongToast(isZH?"已经是最新版本":"Has been the latest version",mContext);
						}
					}
				} catch (JSONException var7) {
					var7.printStackTrace();
				}

			}

			public void sendStart() {
				if (need_toast){
					UpdateManage.this.dialog.setMessage(isZH?"检查更新中":"Check Update");
					UpdateManage.this.dialog.show();
				}
			}

			public void sendLost(String result) {
			}

			public void sendFinish() {
				if (need_toast){
					UpdateManage.this.dialog.dismiss();
				}
			}

			public void sendFailed(String result) {
			}
		});
		connectionTask.execute("http://www.yunrfid.com/CoreSYS.SYS/GetNewAppVer.ajax");
	}

	private void showNoticeDialog(final String apkUrl, String updateMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
		builder.setTitle(this.isZH?"提示":"Notice");
		builder.setMessage(updateMsg);
		builder.setCancelable(false);
		builder.setPositiveButton(this.isZH?"确认":"Confirm", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				(UpdateManage.this.new DownloadTask()).execute(apkUrl);
			}
		});
		builder.show();
	}

	public class DownloadTask extends AsyncTask<String, Integer, Boolean> {
		public DownloadTask() {
		}

		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			UpdateManage.this.dialog.setMessage(values[0] + "%");
		}

		protected void onPreExecute() {
			super.onPreExecute();
			UpdateManage.this.dialog.setMessage("0%");
			UpdateManage.this.dialog.show();
		}

		protected Boolean doInBackground(String... params) {
			try {
				URL e = new URL(params[0]);
				HttpURLConnection conn = (HttpURLConnection)e.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();
				File file = new File("/sdcard/updateApk/");
				if(!file.exists()) {
					file.mkdir();
				}

				UpdateManage.saveFileName = "/sdcard/updateApk/" + DateFormat.format("yyyyMMddkkmmss", System.currentTimeMillis()) + ".apk";
				File ApkFile = new File(UpdateManage.saveFileName);
				FileOutputStream fos = new FileOutputStream(ApkFile);
				int count = 0;
				byte[] buf = new byte[1024];

				while(true) {
					int numRead = is.read(buf);
					count += numRead;
					this.publishProgress(Integer.valueOf((int) ((float) count / (float) length * 100.0F)));
					if(numRead > 0) {
						fos.write(buf, 0, numRead);
						if(!UpdateManage.this.flag) {
							continue;
						}
					}

					fos.close();
					is.close();
					return Boolean.valueOf(!UpdateManage.this.flag);
				}
			} catch (MalformedURLException var12) {
				return Boolean.valueOf(false);
			} catch (IOException var13) {
				return Boolean.valueOf(false);
			}
		}

		protected void onPostExecute(Boolean aBoolean) {
			super.onPostExecute(aBoolean);
			UpdateManage.this.dialog.dismiss();
			if(aBoolean.booleanValue()) {
				File apkFile = new File(UpdateManage.saveFileName);
				if(apkFile.exists()) {
					Intent i = new Intent("android.intent.action.VIEW");
					i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
					UpdateManage.this.mContext.startActivity(i);
				}
			}
		}
	}
}

