package com.hjh.remote_files_sync_app;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import android.util.Log;

public class HLog extends com.hjh.files.sync.common.ILog {

	public static final HLog instance = new HLog();

	private void writeLogFile(String level, String msg) {
		try {
			String path = "";
			if (level.equals("stdout")) {
				path = SyncConfig.StoreStdoutPath;
			} else {
				path = SyncConfig.StoreLogsPath
						+ File.separator
						+ (new java.text.SimpleDateFormat("yyyy_MM_dd",
								Locale.CHINA).format(new Date()));
				path = path + "_" + level + ".txt";
			}
			File log = new File(path);
			if (!log.getParentFile().exists()) {
				log.getParentFile().mkdirs();
			}
			String time = new java.text.SimpleDateFormat("HH:mm:ss",
					Locale.CHINA).format(new Date());
			FileUtils.write(log, "[" + level + "]" + "[" + time + "]" + msg
					+ "\r\n", Charset.forName("utf-8"), true);
			if (level.equals("error")) {
				writeLogFile("stdout", msg);
			}
		} catch (Throwable e) {
			Log.e("hjh", "记录日志发生异常", e);
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
