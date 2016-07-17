package com.hjh.files.sync.common;

public interface RemoteFileManage {

	public RemoteFile[] list(String parent);

	public String md5(String file);

	public byte[] part(String file, int part ,int part_size);

}
