package com.hjh.files.sync.common.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteFileManage;
import com.hjh.files.sync.common.RemoteSyncConfig;
import com.hjh.files.sync.common.util.MD5;
import com.hjh.files.sync.common.util.RemoteFileUtil;

public class RemoteFileManageLocalImpl implements RemoteFileManage {

	private File root;

	public RemoteFileManageLocalImpl(String root_path) {
		Asserts.notNull(root_path, "root_path can not null");
		root = new File(root_path);
		Asserts.check(root.isDirectory(), "is not a folder :" + root_path);
	}

	public RemoteFile[] list(String parentFilePath) {
		File current = toFile(parentFilePath);
		Asserts.check(current.isDirectory(), "is not a folder :" + current.getAbsolutePath());
		File[] list = current.listFiles();
		RemoteFile[] result = new RemoteFile[list == null ? 0 : list.length];
		if (null != list) {
			int i = 0;
			for (File cur : list) {
				result[i++] = toFile(cur);
			}
		}
		return result;
	}

	public String md5(String filePath) {
		try {
			return MD5.md5(toFile(filePath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] part(String filePath, int part, int part_size) {
		RemoteSyncConfig.checkBockSize(part_size);
		long start = part_size * part;
		long end = start + part_size;
		File file = toFile(filePath);
		if (file.length() < start) {
			return new byte[0];
		}
		end = Math.min(end, file.length());
		byte[] cache = new byte[(int) (end - start)];
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			in.skip(start);
			in.read(cache, 0, cache.length);
			return cache;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private File toFile(String filePath) {
		if (null == filePath) {
			return root;
		}
		return new File(root, filePath);
	}

	private RemoteFile toFile(final File file) {
		return new RemoteFile() {

			public String name() {
				return file.getName();
			}

			public String path() {
				return RemoteFileUtil.formatPath(file.getAbsolutePath().substring(root.getAbsolutePath().length()));
			}

			public long length() {
				return file.length();
			}

			public long lastModify() {
				return file.lastModified();
			}

			public boolean isFolder() {
				return file.isDirectory();
			}

		};
	}

}
