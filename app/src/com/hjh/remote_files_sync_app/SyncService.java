package com.hjh.remote_files_sync_app;

import java.io.File;

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
	private boolean is_validate = false;

	@Override
	public void onCreate() {
		HLog.service = this;
		logger.stdout("创建同步服务");
		super.onCreate();
		if (!new File(SyncConfig.StoreConfigPath).exists()) {
			logger.stdout("配置文件不存在:" + SyncConfig.StoreConfigPath);
		} else {
			initClient();
		}
	}

	private void initClient() {
		if (is_validate) {
			Toast.makeText(this, "正在校验数据...", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			client = new ClientForSync(SyncConfig.StoreConfigPath);
		} catch (Throwable e) {
			logger.stdout("初始时client失败:" + SyncConfig.StoreConfigPath + ":"
					+ e.getMessage());
		}
	}

	private void stopClient() {
		if (null != client) {
			client.stop();
			client = null;
			logger.stdout("停止同步任务");
		}
	}

	@Override
	public void onDestroy() {
		logger.stdout("销毁同步服务");
		stopClient();
		HLog.service = null;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String action = intent == null ? null : intent
				.getStringExtra("sync_action");

		if ("start".equals(action)) {
			logger.stdout("触发同步任务");
			if (null == client) {
				initClient();
			}
			if (null == client) {
				Toast.makeText(this, "初始化失败...", Toast.LENGTH_SHORT).show();
			} else if (client.isRunning()) {
				Toast.makeText(this, "数据同步中...", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "成功启动同步任务", Toast.LENGTH_SHORT).show();
			}
			if (null != client && !client.isRunning()) {
				client.start();
			}
		} else if ("stop".equals(action)) {
			logger.stdout("触发停止同步任务");
			this.stopClient();
		} else if ("validate".equals(action)) {
			this.stopClient();
			logger.stdout("触发校验数据任务");
			if (is_validate) {
				Toast.makeText(this, "正在校验数据...", Toast.LENGTH_SHORT).show();
			} else {
				is_validate = true;
				Toast.makeText(this, "启动数据校验...", Toast.LENGTH_SHORT).show();
				new Thread() {
					public void run() {
						try {
							ClientForSync.validate(SyncConfig.StoreConfigPath);
						} catch (Throwable e) {
							logger.stdout("校验数据失败:" + e.getMessage());
						} finally {
							is_validate = false;
						}
					}
				}.start();
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
