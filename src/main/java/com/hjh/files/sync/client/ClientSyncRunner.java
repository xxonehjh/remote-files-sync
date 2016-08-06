package com.hjh.files.sync.client;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.StopAble;
import com.hjh.files.sync.common.thrift.ThriftClientPool;

public class ClientSyncRunner implements StopAble {

	private static ILog logger = HLogFactory.create(ClientFolder.class);

	private ClientForSync client;
	private boolean running = false;
	private boolean stop = false;

	public ClientSyncRunner(ClientForSync clientForSync) {
		this.client = clientForSync;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized void start() {
		if (running) {
			return;
		}
		new Thread() {
			public void run() {
				logger.stdout("#启动同步数据线程");
				running = true;
				stop = false;
				try {
					while (true) {
						if (stop) {
							break;
						} else {
							doSync();
						}
						doSleep();
					}
				} finally {
					try {
						ThriftClientPool.closeAll();
					} finally {
						running = false;
						logger.stdout("#结束同步数据线程");
					}
				}
			}
		}.start();
	}

	protected void doSleep() {
		if (client.getInterval() > 0) {
			try {
				if (client.getInterval() < 1000) {
					Thread.sleep(client.getInterval());
				} else {
					int per = 500;
					long times = client.getInterval() / per;
					for (long i = 0; i < times; i++) {
						if (stop) {
							break;
						}
						Thread.sleep(per);
						if (stop) {
							break;
						}
					}
				}
			} catch (InterruptedException e) {
				logger.error("sleep 异常", e);
			}
		}
	}
	
	public void sync(){
		doSync();
	}

	protected void doSync() {
		for (ClientFolder folder : client.getFolders()) {
			try {
				folder.sync(this);
			} catch (Throwable e) {
				logger.error("同步目录[" + folder.getName() + "]失败!", e);
			}
			if (stop) {
				break;
			}
		}
	}

	public boolean isStop() {
		return stop;
	}

	public void stop() {
		stop = true;
	}

}
