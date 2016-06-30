package com.hjh.files.sync.common.util;

import org.apache.http.util.Asserts;

public class RemoteFileUtil {

	public final static long PART_SIZE = 1024 * 1024;

	public static int countPart(long size) {
		return (int) ((size + PART_SIZE - 1) / PART_SIZE);
	}

	public static String formatPath(String path) {
		Asserts.notBlank(path, "can as blank path :" + path);
		path = path.replace("\\", "/");
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

}
