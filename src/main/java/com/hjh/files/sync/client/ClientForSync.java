package com.hjh.files.sync.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.RemoteSyncConfig;
import com.hjh.files.sync.common.log.LogUtil;
import com.hjh.files.sync.common.thrift.ThriftClientPool;
import com.hjh.files.sync.common.util.PropertiesUtils;

public class ClientForSync {

	static {
		LogUtil.initLog();
	}

	private static final String PORP_KEY_PREFIX = "client.folder.";

	public static void main(String argv[]) throws IOException {

		String prop = "remote_sync.properties";
		if (null != argv && 1 == argv.length) {
			prop = argv[0];
		}

		try {
			new ClientForSync(prop).start();
			/**
			 * new ClientForSync(prop).get("datas").sync(new StopAble() {
			 * 
			 * @Override public boolean isStop() { return false; }
			 * 
			 *           });
			 */
		} finally {
			ThriftClientPool.closeAll();
		}
	}

	private long interval;
	private String store;
	private int block_size;
	private List<ClientFolder> folders;
	private ClientSyncRunner runner;

	public ClientFolder[] getFolders() {
		return folders.toArray(new ClientFolder[folders.size()]);
	}

	public long getInterval() {
		return interval;
	}

	public ClientFolder get(String name) {
		for (ClientFolder item : folders) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}

	public synchronized void start() {
		if (runner.isRunning()) {
			return;
		}
		runner.start();
	}

	public synchronized void stop() {
		runner.stop();
	}

	public ClientForSync(String propPath) throws IOException {
		Properties p = PropertiesUtils.load(propPath);
		RemoteSyncConfig.init(p);

		store = p.getProperty("client.store");

		Asserts.notBlank(store, "can not found config for client.store");
		Asserts.check(new File(store).isDirectory(), "not exist store folder:" + store);

		interval = Long.parseLong(p.getProperty("client.sync.interval", "10000"));

		Asserts.check(interval >= 0, "client.sync.interval must great then 0");

		// 524288 = 1024 * 512
		block_size = Integer.parseInt(p.getProperty("client.block.size", "524288"));

		RemoteSyncConfig.checkBockSize(block_size);
		
		folders = new ArrayList<ClientFolder>();
		for (Object item : p.keySet().toArray()) {
			if (item.toString().startsWith(PORP_KEY_PREFIX)) {
				folders.add(new ClientFolder(item.toString().substring(PORP_KEY_PREFIX.length()), store,
						(String) p.get(item), block_size));
			}
		}

		Asserts.check(folders.size() != 0, "can not find any client folders");

		runner = new ClientSyncRunner(this);
	}

	public int getBlockSize() {
		return block_size;
	}

	public boolean isRunning() {
		return runner.isRunning();
	}

}
