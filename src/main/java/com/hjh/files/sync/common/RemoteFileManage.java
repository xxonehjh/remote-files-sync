package com.hjh.files.sync.common;

public interface RemoteFileManage {

	public RemoteFile[] list(RemoteFile parent);

	public String md5(RemoteFile file);

	public int partCount(RemoteFile file);

	public byte[] part(RemoteFile file, int part);

}
