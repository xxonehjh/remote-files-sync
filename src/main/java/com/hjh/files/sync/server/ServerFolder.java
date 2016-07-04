package com.hjh.files.sync.server;

import com.hjh.files.sync.common.RemoteFileFactory;
import com.hjh.files.sync.common.RemoteFileManage;

public class ServerFolder {

	private String name;
	private String url;

	public ServerFolder(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public RemoteFileManage get() {
		return RemoteFileFactory.queryManage(url);
	}

	
	

}
