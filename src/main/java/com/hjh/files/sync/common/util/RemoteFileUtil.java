package com.hjh.files.sync.common.util;

import java.util.List;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteSyncConfig;

import tutorial.RemoteFileInfo;

public class RemoteFileUtil {
	
	public static long getPartSize(){
		return RemoteSyncConfig.getBlockSize();
	}

	public static int countPart(long size) {
		return (int) ((size + getPartSize() - 1) / getPartSize());
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

	public static RemoteFileInfo to(RemoteFile item) {
		RemoteFileInfo info = new RemoteFileInfo();
		info.setName(item.name());
		info.setPath(item.path());
		info.setLength(item.length());
		info.setLastModify(item.lastModify());
		info.setIsFolder(item.isFolder());
		return info;
	}

	public static RemoteFile[] from(List<RemoteFileInfo> listFiles) {
		if (null == listFiles || 0 == listFiles.size()) {
			return EMPTY;
		}
		int size = listFiles.size();
		RemoteFile[] result = new RemoteFile[listFiles.size()];
		for (int i = 0; i < size; i++) {
			result[i] = from(listFiles.get(i));
		}
		return result;
	}

	public static RemoteFile from(final RemoteFileInfo info) {
		return new RemoteFile() {

			@Override
			public String name() {
				return info.getName();
			}

			@Override
			public String path() {
				return info.getPath();
			}

			@Override
			public long length() {
				return info.getLength();
			}

			@Override
			public long lastModify() {
				return info.getLastModify();
			}

			@Override
			public boolean isFolder() {
				return info.isIsFolder();
			}
		};
	}

}
