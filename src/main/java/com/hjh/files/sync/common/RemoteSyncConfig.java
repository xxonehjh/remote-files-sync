package com.hjh.files.sync.common;

import java.util.Properties;

public class RemoteSyncConfig {
	
	private static final long max_block_size = 1024 * 1024 * 5;
	
	public static void checkBockSize(int block_size){
		if (block_size <= 0) {
			throw new RuntimeException("part_size must great then 0 K");
		}

		if (block_size > RemoteSyncConfig.max_block_size) {
			throw new RuntimeException(
					"part_size must less or equal then " + (RemoteSyncConfig.max_block_size / 1024) + " K");
		}
	}

	private static long min_diff_time = 1000;
	
	private static int timeout = 1000 * 60 * 5;

	public static long getMinDiffTime() {
		return min_diff_time;
	}

	public static int getTimeout() {
		return timeout;
	}

	public static void init(Properties p) {

		min_diff_time = Long.parseLong(p.getProperty("config.min.diff.time", "1000"));
		
		RemoteFileFactory.setTruststore(p.getProperty("client.truststore"));
		
		if(p.contains("config.timeout")){
			timeout = Integer.parseInt(p.getProperty("config.timeout"));
		}
	}
	
	

}
