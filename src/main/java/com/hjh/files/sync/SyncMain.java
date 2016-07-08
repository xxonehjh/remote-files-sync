package com.hjh.files.sync;

import java.io.IOException;

import org.apache.http.util.Asserts;
import org.apache.thrift.transport.TTransportException;

import com.hjh.files.sync.client.ClientForSync;
import com.hjh.files.sync.server.ServerForSync;

public class SyncMain {

	public static void main(String argvs[]) throws TTransportException, IOException {
		Asserts.check(argvs != null && argvs.length == 2, "require params type and config path");
		String type = argvs[0];
		String config = argvs[1];

		if ("server".equals(type)) {
			ServerForSync.main(new String[] { config });
		} else if ("client".equals(type)) {
			ClientForSync.main(new String[] { config });
		} else {
			throw new RuntimeException("unknow type " + type);
		}
	}

}
