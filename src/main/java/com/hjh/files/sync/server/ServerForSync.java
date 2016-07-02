package com.hjh.files.sync.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.log.LogUtil;
import com.hjh.files.sync.common.util.PropertiesUtils;

public class ServerForSync {

	private static final String PORP_KEY_PREFIX = "server.folder.";

	public static void main(String argv[]) throws IOException {

		LogUtil.initLog();

		String prop = "remote_sync_for_server.properties";
		if (null != argv && 1 == argv.length) {
			prop = argv[0];
		}
		ServerForSync server = new ServerForSync(prop);
		server.start();

	}

	private int port;
	private List<ServerFolder> folders;

	public ServerForSync(String propPath) throws IOException {
		Properties p = PropertiesUtils.load(propPath);

		port = Integer.parseInt(p.getProperty("server.port"));

		folders = new ArrayList<ServerFolder>();
		for (Object item : p.keySet().toArray()) {
			if (item.toString().startsWith(PORP_KEY_PREFIX)) {
				folders.add(
						new ServerFolder(item.toString().substring(PORP_KEY_PREFIX.length()), (String) p.get(item)));
			}
		}

		Asserts.check(folders.size() != 0, "can not find any server folders");
	}

	public void start() {

	}

}
