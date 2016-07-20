package com.hjh.remote_files_sync_app;

import java.io.File;
import java.util.Locale;

import org.apache.thrift.transport.TTransportException;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.ILogFactory;
import com.hjh.files.sync.server.ServerFolder;
import com.hjh.files.sync.server.ServerForSync;

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
	private boolean touchStop = false;
	private ServerForSync server;

	@Override
	public void onCreate() {
		HLog.service = this;
		super.onCreate();
		if (!new File(SyncConfig.StoreConfigPath).exists()) {
			logger.stdout("配置文件不存在:" + SyncConfig.StoreConfigPath);
		}
	}

	private synchronized void startServer(String ip) {
		if (null == ip) {
			logger.stdout("获取不到wifi IP");
			return;
		}
		if (null == server) {
			try {
				server = new ServerForSync(SyncConfig.StoreConfigPath);
				touchStop = false;
				logger.stdout("服务启动[" + ip + ":" + server.getPort() + "]");
				new Thread() {
					public void run() {
						try {
							server.start();
						} catch (TTransportException e) {
							logger.error("启动服务失败", e);
						} finally {
							touchStop = false;
							server = null;
							logger.stdout("服务已经停止");
							updateServerInfo();
						}
					}
				}.start();
			} catch (Throwable e) {
				logger.stdout("初始化server失败:" + SyncConfig.StoreConfigPath + ":"
						+ e.getMessage());
			}
		}
	}

	private synchronized void stopServer() {
		if (touchStop) {
			return;
		}
		if (null != server) {
			touchStop = true;
			new Thread() {
				public void run() {
					logger.stdout("启动停止服务线程");
					server.stop();
				}
			}.start();
		}
	}

	@Override
	public void onDestroy() {
		stopServer();
		HLog.service = null;
		super.onDestroy();
	}

	private void updateServerInfo() {
		StringBuffer serverInfo = new StringBuffer();
		if (null == server) {
			serverInfo.append("服务未启动");
		} else {
			String ip = getLocalIpAddress();
			if (null == ip) {
				serverInfo.append("无法获取Wifi Ip");
			} else {
				serverInfo.append("服务地址[" + ip + ":" + server.getPort()
						+ "]\r\n");
				for (ServerFolder folder : server.getFolders()) {
					serverInfo.append("目录[" + folder.getName() + "]\r\n=>"
							+ folder.getUrl() + "\r\n");
				}
			}
		}
		HLog.sendServerInfo(serverInfo.toString());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent == null ? null : intent
				.getStringExtra("sync_action");
		if ("start".equals(action)) {
			if (null != server) {
				logger.stdout("服务已经启动");
			} else {
				String ip = getLocalIpAddress();
				startServer(ip);
			}
			updateServerInfo();
		} else if ("stop".equals(action)) {
			logger.stdout("触发停止任务");
			this.stopServer();
		} else if ("get_server_info".equals(action)) {
			updateServerInfo();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private String getLocalIpAddress() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		if (0 == ipAddress) {
			return null;
		}
		return String.format(Locale.CHINA, "%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
	}

}
