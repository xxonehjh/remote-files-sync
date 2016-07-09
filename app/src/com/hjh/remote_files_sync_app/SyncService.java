package com.hjh.remote_files_sync_app;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.hjh.files.sync.client.ClientForSync;
import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.ILogFactory;

public class SyncService extends Service {

	public static final String ACTION = "com.hjh.remote_files_sync_app.SyncService";

	static {

		HLogFactory.setInstance(new ILogFactory() {

			@Override
			public ILog create(Class<?> type) {
				return HLog.instance;
			}

		});
	}

	private static ILog logger = HLogFactory.create(SyncService.class);
	private ClientForSync client;

	@Override
	public void onCreate() {
		HLog.service = this;
		logger.stdout("创建同步服务进程");
		super.onCreate();
		if (!new File(SyncConfig.StoreConfigPath).exists()) {
			logger.stdout("配置文件不存在:" + SyncConfig.StoreConfigPath);
		} else {
			try {
				client = new ClientForSync(SyncConfig.StoreConfigPath);
			} catch (IOException e) {
				logger.stdout("初始时client失败:" + SyncConfig.StoreConfigPath + ":"
						+ e.getMessage());
			}
		}

	}

	@Override
	public void onDestroy() {
		logger.stdout("销毁同步服务进程");
		if (null != client) {
			client.stop();
		}
		HLog.service = null;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null != intent && intent.getBooleanExtra("sync", false)) {
			logger.stdout("触发同步服务");
			if (null == client) {
				Toast.makeText(this, "未成功初始时 client ...", Toast.LENGTH_SHORT)
						.show();
			} else if (client.isRunning()) {
				Toast.makeText(this, "数据同步中...", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "成功启动同步数据服务", Toast.LENGTH_SHORT).show();
				client.start();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
