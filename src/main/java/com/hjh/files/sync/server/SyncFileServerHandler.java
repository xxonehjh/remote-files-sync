package com.hjh.files.sync.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.util.RemoteFileUtil;

import tutorial.RemoteFileInfo;
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
	public ByteBuffer part(String folder, String path, long part, long part_size) throws TException {
		logger.info(String.format("part [%s] [%s] [%d]", folder, path, part));
		byte[] partData = sync.get(folder).part(path, part, part_size);
		logger.info(String.format("send part data %d", partData.length));
		return ByteBuffer.wrap(partData);
	}

	@Override
	public List<RemoteFileInfo> listFiles(String folder, String path) throws TException {
		logger.info(String.format("list files [%s] [%s]", folder, path == null ? "ROOT" : path));
		List<RemoteFileInfo> result = new ArrayList<RemoteFileInfo>();
		RemoteFile[] files = sync.get(folder).list(path);
		if (null != files) {
			for (RemoteFile item : files) {
				result.add(RemoteFileUtil.to(item));
			}
		}
		return result;
	}

	//////////////////////////////////////////////

	@Override
	public void ping() throws TException {
	}

}
