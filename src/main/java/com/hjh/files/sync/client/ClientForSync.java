package com.hjh.files.sync.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.log.LogUtil;
import com.hjh.files.sync.common.util.PropertiesUtils;

public class ClientForSync {

	private static final String PORP_KEY_PREFIX = "client.folder.";

	public static void main(String argv[]) throws IOException {
		LogUtil.initLog();

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
		Properties p = PropertiesUtils.load(propPath);

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

		Asserts.check(folders.size() != 0, "can not find any client folders");
	}

}
