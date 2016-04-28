package com.huiwu.model.http;

/**
 * Created by HuiWu on 2015/9/23.
 */
public interface ConnectionHandler {
	void sendStart();

	void sendFinish();

	void sendFailed(String result);

	void sendSuccess(String result);

	void sendLost(String result);
}
