package com.hjh.files.sync.common;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.local.RemoteFileManageLocalImpl;
import com.hjh.files.sync.common.thrift.RemoteFileManageThriftImpl;

public class RemoteFileFactory {

	public static RemoteFileManage queryManage(String url) {
		if (url.startsWith("from:")) {
			return createByFrom(url);
		}
		return new RemoteFileManageLocalImpl(url);
	}

	private static RemoteFileManage createByFrom(String url) {
		String[] url_subs_folder = url.split("/");
		String[] url_subs_ip = url_subs_folder[0].split(":");

		Asserts.check(url_subs_folder.length == 2, "Error url(can not find part folder):" + url);
		Asserts.check(url_subs_ip.length == 3, "Error url(can not find part ip and port):" + url);
		Asserts.check(url_subs_ip[0].equals("from"), "Error url(unknown type):" + url);

		String ip = url_subs_ip[1];
		int port = Integer.parseInt(url_subs_ip[2]);
		String folder = url_subs_folder[1];

		return new RemoteFileManageThriftImpl(ip, port, folder);
	}

}
