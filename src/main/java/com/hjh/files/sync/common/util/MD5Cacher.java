package com.hjh.files.sync.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MD5Cacher {

	private static class CacheItem {
		public long length;
		public long time;
		public String md5;

		public void update(File file) throws IOException {
			time = file.lastModified();
			length = file.length();
			md5 = MD5.md5(file);
		}
	}

	private static Map<String, CacheItem> cache = Collections.synchronizedMap(new HashMap<String, CacheItem>());

	public static String md5(File file) throws IOException {
		String key = MD5.md5(file.getAbsolutePath());
		CacheItem item = cache.get(key);
		if (null != item) {
			if (item.time == file.lastModified() && item.length == file.length()) {
				return item.md5;
			} else {
				cache.remove(key);
			}
		}
		if (item == null) {
			item = new CacheItem();
			item.update(file);
		} else {
			item.update(file);
		}
		cache.put(key, item);
		return item.md5;
	}

}
