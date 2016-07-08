package com.hjh.files.sync.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.util.Asserts;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.RemoteFileManage;
import com.hjh.files.sync.common.RemoteSyncConfig;
import com.hjh.files.sync.common.log.LogUtil;
import com.hjh.files.sync.common.thrift.ThriftClientPool;
import com.hjh.files.sync.common.util.PropertiesUtils;

import tutorial.SyncFileServer;

public class ServerForSync {

	static {
		LogUtil.initLog();
	}

	private static ILog logger = HLogFactory.create(ServerForSync.class);
	private static final String PORP_KEY_PREFIX = "server.folder.";

	public static void main(String argv[]) throws IOException, TTransportException {

		String prop = "remote_sync.properties";
		if (null != argv && 1 == argv.length) {
			prop = argv[0];
		}

		try {
			ServerForSync server = new ServerForSync(prop);
			server.start();
		} finally {
			ThriftClientPool.closeAll();
		}

	}

	private int port;
	private String keystore;
	private Map<String, ServerFolder> folders;

	public ServerForSync(String propPath) throws IOException {
		Properties p = PropertiesUtils.load(propPath);
		RemoteSyncConfig.init(p);
		port = Integer.parseInt(p.getProperty("server.port"));
		keystore = p.getProperty("server.keystore");

		folders = new HashMap<String, ServerFolder>();
		for (Object item : p.keySet().toArray()) {
			if (item.toString().startsWith(PORP_KEY_PREFIX)) {
				ServerFolder cur = new ServerFolder(item.toString().substring(PORP_KEY_PREFIX.length()),
						(String) p.get(item));
				folders.put(cur.getName(), cur);
			}
		}

		Asserts.check(folders.size() != 0, "can not find any server folders");
	}

	public void start() throws TTransportException {

		SyncFileServerHandler handler = new SyncFileServerHandler(this);
		SyncFileServer.Processor<SyncFileServerHandler> processor = new SyncFileServer.Processor<SyncFileServerHandler>(
				handler);

		if (null == this.keystore) {
			simple(processor, port);
		} else {
			secure(processor, port, keystore);
		}
	}

	public static void simple(SyncFileServer.Processor<SyncFileServerHandler> processor, int port)
			throws TTransportException {

		TServerTransport serverTransport = new TServerSocket(port);
		TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

		// Use this for a multithreaded server
		// TServer server = new TThreadPoolServer(new
		// TThreadPoolServer.Args(serverTransport).processor(processor));

		logger.info("Starting the simple server...");
		server.serve();
	}

	public static void secure(SyncFileServer.Processor<SyncFileServerHandler> processor, int port,
			String keystoreConfig) throws TTransportException {
		/*
		 * Use TSSLTransportParameters to setup the required SSL parameters. In
		 * this example we are setting the keystore and the keystore password.
		 * Other things like algorithms, cipher suites, client auth etc can be
		 * set.
		 */
		TSSLTransportParameters params = new TSSLTransportParameters();

		String keystoreConfigArr[] = keystoreConfig.split("@");
		String keystore = keystoreConfigArr[0];
		logger.info("user key:" + keystore);
		Asserts.check(new File(keystore).exists(), "can not find :" + keystore);

		// The Keystore contains the private key
		params.setKeyStore(keystore, keystoreConfigArr[1], null, null);

		/*
		 * Use any of the TSSLTransportFactory to get a server transport with
		 * the appropriate SSL configuration. You can use the default settings
		 * if properties are set in the command line. Ex:
		 * -Djavax.net.ssl.keyStore=.keystore and
		 * -Djavax.net.ssl.keyStorePassword=thrift
		 * 
		 * Note: You need not explicitly call open(). The underlying server
		 * socket is bound on return from the factory class.
		 */
		TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(port, 0, null, params);
		// TServer server = new TSimpleServer(new
		// Args(serverTransport).processor(processor));
		TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).minWorkerThreads(1)
				.maxWorkerThreads(3).processor(processor));

		// Use this for a multi threaded server
		// TServer server = new TThreadPoolServer(new
		// TThreadPoolServer.Args(serverTransport).processor(processor));

		logger.info("Starting the secure server...");
		server.serve();
	}

	public RemoteFileManage get(String folder) {
		return this.folders.get(folder).get();
	}

}
