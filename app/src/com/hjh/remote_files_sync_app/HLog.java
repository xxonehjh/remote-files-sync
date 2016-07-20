package com.hjh.remote_files_sync_app;

import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

public class HLog extends com.hjh.files.sync.common.ILog {

	public static final String RECEIVER_ID = "com.hjh.Sync.communication.RECEIVER";

	public static final HLog instance = new HLog();

	private static Intent intent = new Intent(RECEIVER_ID);

	private static Intent intent_for_server_info = new Intent(RECEIVER_ID);

	public static Service service;

	private void writeLogFile(String level, String msg) {
		String time = new java.text.SimpleDateFormat("HH:mm:ss", Locale.CHINA)
				.format(new Date());
		String full_msg = "[" + level + "]" + "[" + time + "]" + msg + "\r\n";
		if (null != service) {
			intent.putExtra("log", full_msg);
			service.sendBroadcast(intent);
		}
	}

	public static void sendServerInfo(String info) {
		if (null != service) {
			intent_for_server_info.putExtra("server_info", info);
			service.sendBroadcast(intent_for_server_info);
		}
	}

	public void debug(String msg) {
		writeLogFile("debug", msg);
		Log.d("hjh", msg);
	}

	public void info(String msg) {
		writeLogFile("info", msg);
		Log.i("hjh", msg);
	}

	public void error(String msg, Throwable e) {
		writeLogFile("error", msg + "\r\n" + e.getMessage());
		Log.e("hjh", msg, e);
	}

	@Override
	public void stdout(String msg) {
		writeLogFile("stdout", msg);
		Log.i("hjh", msg);
	}

}
