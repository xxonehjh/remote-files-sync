package com.hjh.files.sync.common;

import java.util.Properties;

public class RemoteSyncConfig {

	private static long min_diff_time = 800;

	public static long getMinDiffTime() {
		return min_diff_time;
	}

	public static void init(Properties p) {

		min_diff_time = Long.parseLong(p.getProperty("config.min.diff.time", "800"));
		
		RemoteFileFactory.setTruststore(p.getProperty("client.truststore"));
	}

}
