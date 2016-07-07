package com.hjh.remote_files_sync_app;

import java.io.File;

import android.os.Environment;

public class SyncConfig {

	public final static String StorePath = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "hjh_datas_sync";

	public final static String StoreConfigPath = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "hjh_datas_sync" + File.separator + "config.properties";

	public final static String StoreLogsPath = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "hjh_datas_sync" + File.separator + "logs";

	public final static String StoreStdoutPath = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "hjh_datas_sync" + File.separator + "stdout.txt";

	static {
		if (!new File(StorePath).exists()) {
			new File(StorePath).mkdir();
		}
	}

}
