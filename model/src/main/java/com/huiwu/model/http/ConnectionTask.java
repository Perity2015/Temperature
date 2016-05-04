package com.huiwu.model.http;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class ConnectionTask extends AsyncTask<String, Integer, String> {
	private String request_url;

	private Map<String, String> request_map;

	private Map<String, File> request_file_map;

	private boolean need_file = true;

	private ConnectionHandler handler;

	private ConnectionUtil connectionUtil;

	public ConnectionTask(Map<String, String> request_map, ConnectionHandler handler) {
		this.request_map = request_map;
		this.request_file_map = null;
		this.need_file = true;
		this.handler = handler;
		this.connectionUtil = new ConnectionUtil();
	}

	public ConnectionTask(Map<String, String> request_map, Map<String, File> request_file_map, ConnectionHandler handler) {
		this.request_map = request_map;
		this.request_file_map = request_file_map;
		this.need_file = true;
		this.handler = handler;
		this.connectionUtil = new ConnectionUtil();
	}

	public ConnectionTask(Map<String, String> request_map, Map<String, File> request_file_map, boolean need_file, ConnectionHandler handler) {
		this.request_map = request_map;
		this.request_file_map = request_file_map;
		this.need_file = need_file;
		this.handler = handler;
		this.connectionUtil = new ConnectionUtil();
	}

	public ConnectionTask(Map<String, String> request_map, Map<String, File> request_file_map, boolean need_file, ConnectionHandler handler,ConnectionUtil connectionUtil){
		this.request_map = request_map;
		this.request_file_map = request_file_map;
		this.need_file = need_file;
		this.handler = handler;
		this.connectionUtil = connectionUtil;
	}

	protected void onPreExecute() {
		super.onPreExecute();
		this.handler.sendStart();
	}

	protected String doInBackground(String... params) {
		this.request_url = params[0];

		Log.d("TAG",String.valueOf(request_map));

		try {
			return connectionUtil.postParams(this.request_url, this.request_map, this.request_file_map, this.need_file);
		} catch (IOException var3) {
			var3.printStackTrace();
			return null;
		}
	}

	protected void onPostExecute(String s) {
		super.onPostExecute(s);
		this.handler.sendFinish();
		if(s == null) {
			this.handler.sendFailed(s);
		} else if (TextUtils.equals(s,"clear")){
			this.handler.sendLost(s);
		}else {
			try {
				this.handler.sendSuccess(s);
			}catch (Exception e){
				this.handler.sendFailed(s);
			}
		}

	}
}

