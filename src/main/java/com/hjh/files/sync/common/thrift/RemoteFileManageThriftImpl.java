package com.hjh.files.sync.common.thrift;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteFileManage;

public class RemoteFileManageThriftImpl implements RemoteFileManage {

	public RemoteFileManageThriftImpl(String ip, int port, String folder) {

	}

	public RemoteFile[] list(RemoteFile parent) {
		return null;
	}

	public String md5(RemoteFile file) {
		return null;
	}

	public int partCount(RemoteFile file) {
		return 0;
	}

	public byte[] part(RemoteFile file, int part) {
		return null;
	}

}
