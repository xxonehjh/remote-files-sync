package com.hjh.files.sync.common;

public interface RemoteFile {
	
	public String name();
	
	public String path();
	
	public long length();
	
	public long lastModify();
	
	public boolean isFolder();

}
