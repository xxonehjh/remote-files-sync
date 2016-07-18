package com.hjh.files.sync.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.util.MD5;

public class FileInfoRecorder {

	private File root;

	public FileInfoRecorder(ClientFolder folder) {
		root = new File(new File(new File(folder.getStore_folder()), ".info"), folder.getName());
		if (!root.isDirectory()) {
			Asserts.check(root.mkdirs(), "can not create folder:" + root.getAbsolutePath());
		}
	}

	private String toString(RemoteFile file) {
		return String.format("%s--%s--%d--%d--%s", file.name(), file.path(), file.lastModify(), file.length(),
				file.isFolder() ? "1" : "0");
	}

	public boolean isSame(RemoteFile file) throws IOException {
		String key = MD5.md5(file.path());
		File info = new File(root, key);
		if (info.isFile()) {
			return FileUtils.readFileToString(info, "utf-8").equals(toString(file));
		}
		return false;
	}

	public void record(RemoteFile file) throws IOException {
		String key = MD5.md5(file.path());
		File info = new File(root, key);
		FileUtils.writeStringToFile(info, toString(file), "utf-8");
	}

}
