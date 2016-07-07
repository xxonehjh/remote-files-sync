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
import com.hjh.files.sync.common.thrift.ThriftClientPool;

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
	private boolean running = false;

	@Override
	public void onCreate() {
		logger.stdout("创建同步服务进程");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		logger.stdout("销毁同步服务进程");
		ThriftClientPool.closeAll();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null != intent && intent.getBooleanExtra("sync", false)) {
			logger.stdout("触发同步服务");
			if (running) {
				Toast.makeText(this, "数据同步中...", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "成功启动同步数据服务", Toast.LENGTH_SHORT).show();
				final File config = new File(SyncConfig.StoreConfigPath);
				if (config.isFile()) {
					new Thread() {
						public void run() {
							try {
								ClientForSync.main(new String[] { config
										.getCanonicalPath() });
							} catch (final Throwable e) {
								logger.error("同步失败", e);
							} finally {
								logger.stdout("[[同步线程结束]]");
							}
						}
					}.start();
				} else {
					logger.stdout("配置文件不存在:" + SyncConfig.StoreConfigPath);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
