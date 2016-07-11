package com.hjh.files.sync.common;

import java.util.Properties;

public class RemoteSyncConfig {

	private static long min_diff_time = 800;
	
	private static long block_size = 1024*512;
	
	private static int timeout = 1000 * 60 * 5;

	public static long getMinDiffTime() {
		return min_diff_time;
	}

	public static long getBlockSize() {
		return block_size;
	}

	public static int getTimeout() {
		return timeout;
	}

	public static void init(Properties p) {

		min_diff_time = Long.parseLong(p.getProperty("config.min.diff.time", "800"));
		
		RemoteFileFactory.setTruststore(p.getProperty("client.truststore"));
		
		if(p.contains("config.block.size")){
			block_size = Long.parseLong(p.getProperty("config.block.size"));
		}
		
		if(p.contains("config.timeout")){
			timeout = Integer.parseInt(p.getProperty("config.timeout"));
		}
	}
	
	

}
