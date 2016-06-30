package com.hjh.files.sync.common.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteFileManage;
import com.hjh.files.sync.common.util.MD5;
import com.hjh.files.sync.common.util.RemoteFileUtil;

public class RemoteFileManageLocalImpl implements RemoteFileManage {

	private File root;

	public RemoteFileManageLocalImpl(String root_path) {
		Asserts.notNull(root_path, "root_path can not null");
		root = new File(root_path);
		Asserts.check(root.isDirectory(), "is not a folder :" + root_path);
	}

	public RemoteFile[] list(RemoteFile parent) {
		File current = toFile(parent);
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

	public String md5(RemoteFile file) {
		try {
			return MD5.md5(toFile(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int partCount(RemoteFile file) {
		return RemoteFileUtil.countPart(file.length());
	}

	public byte[] part(RemoteFile file, int part) {
		long start = RemoteFileUtil.PART_SIZE * part;
		long end = start + RemoteFileUtil.PART_SIZE;
		if (file.length() < start) {
			return new byte[0];
		}
		end = Math.min(end, file.length());
		byte[] cache = new byte[(int) (end - start)];
		FileInputStream in = null;
		try {
			in = new FileInputStream(toFile(file));
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

	private File toFile(RemoteFile file) {
		if (null == file) {
			return root;
		}
		return new File(root, file.path());
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
