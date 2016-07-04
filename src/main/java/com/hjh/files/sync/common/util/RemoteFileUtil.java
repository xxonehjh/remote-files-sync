package com.hjh.files.sync.common.util;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.RemoteFile;

public class RemoteFileUtil {

	private static ILog logger = HLogFactory.create(RemoteFileUtil.class);

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

	private static final RemoteFile[] EMPTY = new RemoteFile[0];
	private static final String FileSep = "_fs_";
	private static final String ContentSep = "_cs_";
	private static final String ContentStr = "%s" + ContentSep + "%s" + ContentSep + "%s" + ContentSep + "%s"
			+ ContentSep + "%s";

	public static String toString(RemoteFile[] files) {
		if (null == files || 0 == files.length) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		for (RemoteFile item : files) {
			if (buf.length() != 0) {
				buf.append(FileSep);
			}
			buf.append(toString(item));
		}
		return buf.toString();
	}

	public static String toString(RemoteFile file) {
		return String.format(ContentStr, file.name(), file.path(), "" + file.length(), "" + file.lastModify(),
				"" + file.isFolder());
	}

	public static RemoteFile[] parseRemoteFiles(String remote_files_str) {
		if (null == remote_files_str || 0 == remote_files_str.length()) {
			return EMPTY;
		}
		String[] items = remote_files_str.split(FileSep);
		RemoteFile[] files = new RemoteFile[items.length];
		for (int i = 0; i < items.length; i++) {
			files[i] = parseRemoteFile(items[i]);
		}
		return files;
	}

	public static RemoteFile parseRemoteFile(String remote_file_str) {
		if (null == remote_file_str || 0 == remote_file_str.length()) {
			return null;
		}
		logger.debug("parse:" + remote_file_str);
		final String arr[] = remote_file_str.split(ContentSep);
		return new RemoteFile() {

			@Override
			public String name() {
				return arr[0];
			}

			@Override
			public String path() {
				return arr[1];
			}

			@Override
			public long length() {
				return Long.parseLong(arr[2]);
			}

			@Override
			public long lastModify() {
				return Long.parseLong(arr[3]);
			}

			@Override
			public boolean isFolder() {
				return "true".equals(arr[4]);
			}

		};
	}

}
