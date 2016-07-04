package com.hjh.files.sync.server;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.util.RemoteFileUtil;

import shared.SharedStruct;
import tutorial.SyncFileServer;

public class SyncFileServerHandler implements SyncFileServer.Iface {

	private static ILog logger = HLogFactory.create(ServerForSync.class);
	private ServerForSync sync;

	public SyncFileServerHandler(ServerForSync serverForSync) {
		this.sync = serverForSync;
	}

	@Override
	public String md5(String folder, String path) throws TException {
		logger.info(String.format("md5 [%s] [%s]", folder, path));
		return sync.get(folder).md5(path);
	}

	@Override
	public int partCount(String folder, long length) throws TException {
		logger.info(String.format("partCount [%s] [%d]", folder, length));
		return sync.get(folder).partCount(length);
	}

	@Override
	public ByteBuffer part(String folder, String path, int part) throws TException {
		logger.info(String.format("part [%s] [%s] [%d]", folder, path, part));
		byte[] partData = sync.get(folder).part(path, part);
		logger.info(String.format("send part data %d", partData.length));
		return ByteBuffer.wrap(partData);
	}

	@Override
	public String listFiles(String folder, String path) throws TException {
		logger.info(String.format("list files [%s] [%s]", folder, path));
		String result = RemoteFileUtil.toString(sync.get(folder).list(path));
		return result;
	}

	//////////////////////////////////////////////

	@Override
	public SharedStruct getStruct(int key) throws TException {
		return null;
	}

	@Override
	public void ping() throws TException {
	}

}
