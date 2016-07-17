package com.hjh.files.sync.common.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteFileManage;
import com.hjh.files.sync.common.util.RemoteFileUtil;

import tutorial.SyncFileServer;

public class RemoteFileManageThriftImpl implements RemoteFileManage {

	private String folder;
	private String ip;
	private int port;

	public RemoteFileManageThriftImpl(String ip, int port, String folder, String truststore)
			throws TTransportException {

		this.ip = ip;
		this.port = port;
		this.folder = folder;

		ThriftClientPool.reg(ip, port, truststore);
	}

	private SyncFileServer.Client client() {
		return ThriftClientPool.get(ip, port);
	}

	public RemoteFile[] list(String parent) {
		try {
			return RemoteFileUtil.from(client().listFiles(folder, parent));
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	public String md5(String file) {
		try {
			return client().md5(folder, file);
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] part(String file, int part, int part_size)
	{
		try {
			return client().part(folder, file, part, part_size).array();
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

}
