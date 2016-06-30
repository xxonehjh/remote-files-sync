package com.hjh.files.sync.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.ILogFactory;

public class ClientForSync {

	private static final String PORP_KEY_PREFIX = "client.folder.";

	static {
		HLogFactory.setInstance(new ILogFactory() {

			public ILog create(Class<?> type) {

				final Log logger = LogFactory.getLog(type);

				return new ILog() {

					@Override
					public void debug(String msg) {
						logger.debug(msg);
					}

					@Override
					public void info(String msg) {
						logger.info(msg);
					}

					@Override
					public void error(String msg, Throwable e) {
						logger.error(msg, e);
					}

				};
			}

		});
	}

	public static void main(String argv[]) throws IOException {
		String prop = "remote_sync_for_client.properties";
		if (null != argv && 1 == argv.length) {
			prop = argv[0];
		}
		ClientForSync client = new ClientForSync(prop);
		client.get("local").sync();
	}

	private String store;
	private List<ClientFolder> folders;

	private ClientFolder get(String name) {
		for (ClientFolder item : folders) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}

	public ClientForSync(String propPath) throws IOException {
		Properties p = new Properties();
		if (new File(propPath).exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(new File(propPath));
				p.load(in);
			} finally {
				if (null != in) {
					in.close();
				}
			}
		} else {
			p.load(this.getClass().getClassLoader().getResourceAsStream(propPath));
		}

		store = p.getProperty("client.store");

		Asserts.notBlank(store, "can not found config for client.store");
		Asserts.check(new File(store).isDirectory(), "not exist store folder:" + store);

		folders = new ArrayList<ClientFolder>();
		for (Object item : p.keySet().toArray()) {
			if (item.toString().startsWith(PORP_KEY_PREFIX)) {
				folders.add(new ClientFolder(item.toString().substring(PORP_KEY_PREFIX.length()), store,
						(String) p.get(item)));
			}
		}

		Asserts.check(folders.size() != 0, "can not find any folders");
	}

}
