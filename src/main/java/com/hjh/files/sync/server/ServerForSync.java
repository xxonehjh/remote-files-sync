package com.hjh.files.sync.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.util.Asserts;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
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

	private String type;
	private int port;
	private String keystore;
	private Map<String, ServerFolder> folders;
	private TServer tserver;

	public int getPort() {
		return port;
	}

	public ServerFolder[] getFolders() {
		return folders.values().toArray(new ServerFolder[folders.size()]);
	}

	public ServerForSync(String propPath) throws IOException {
		Properties p = PropertiesUtils.load(propPath);
		RemoteSyncConfig.init(p);
		port = Integer.parseInt(p.getProperty("server.port", "9958"));
		type = p.getProperty("server.type", "simple");
		keystore = p.getProperty("server.keystore");

		folders = new HashMap<String, ServerFolder>();
		for (Object item : p.keySet().toArray()) {
			if (item.toString().startsWith(PORP_KEY_PREFIX)) {
				ServerFolder cur = new ServerFolder(item.toString().substring(PORP_KEY_PREFIX.length()),
						(String) p.get(item));
				folders.put(cur.getName(), cur);
				logger.stdout("服务目录:" + cur.getName() + ":From:" + cur.getUrl());
			}
		}

		if (folders.size() == 0) {
			logger.info("can not find any server folders");
		}
	}

	public void stop() {
		if (null != tserver) {
			synchronized (this) {
				if (null != tserver) {
					try {
						logger.stdout("停止server");
						tserver.stop();
						logger.stdout("停止server ok");
					} finally {
						tserver = null;
					}
				}
			}
		}
	}

	public void start() throws TTransportException {

		synchronized (this) {
			if (null != tserver) {
				throw new RuntimeException("Server is start!");
			}

			SyncFileServerHandler handler = new SyncFileServerHandler(this);
			SyncFileServer.Processor<SyncFileServerHandler> processor = new SyncFileServer.Processor<SyncFileServerHandler>(
					handler);

			if (null == this.keystore) {
				tserver = simple(processor, port, type);
			} else {
				tserver = secure(processor, port, type, keystore);
			}
		}

		tserver.serve();
	}

	public static TServer simple(SyncFileServer.Processor<SyncFileServerHandler> processor, int port, String type)
			throws TTransportException {

		TServer server;
		TServerTransport transport;

		if ("simple".equals(type)) {
			transport = new TServerSocket(port, RemoteSyncConfig.getTimeout());
			server = new TSimpleServer(new TSimpleServer.Args(transport).processor(processor));
		} else if ("mult_thread".equals(type)) {
			transport = new TServerSocket(port, RemoteSyncConfig.getTimeout());
			server = new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor));
		} else if ("nio".equals(type)) {
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port,
					RemoteSyncConfig.getTimeout());
			server = new THsHaServer(new THsHaServer.Args(serverTransport).processor(processor));
			transport = serverTransport;
		} else {
			throw new RuntimeException("can not create server by type:" + type);
		}

		logger.info("Starting the simple server...");
		return server;
	}

	public static TServer secure(SyncFileServer.Processor<SyncFileServerHandler> processor, int port, String type,
			String keystoreConfig) throws TTransportException {

		TServer server;
		TServerTransport transport;

		/*
		 * Use TSSLTransportParameters to setup the required SSL parameters. In
		 * this example we are setting the keystore and the keystore password.
		 * Other things like algorithms, cipher suites, client auth etc can be
		 * set.
		 */
		TSSLTransportParameters params = new TSSLTransportParameters();

		String keystoreConfigArr[] = keystoreConfig.split("@");

		if (keystoreConfigArr.length != 2) {
			throw new RuntimeException("server.keystore 格式错误(缺少 @密码):" + keystoreConfig);
		}

		String keystore = keystoreConfigArr[0];
		logger.info("user key:" + keystore);
		Asserts.check(new File(keystore).exists(), "can not find :" + keystore);

		// The Keystore contains the private key
		if (keystore.endsWith(".bks")) {
			params.setKeyStore(keystore, keystoreConfigArr[1], "X509", "BKS");
		} else {
			params.setKeyStore(keystore, keystoreConfigArr[1], null, null);
		}

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
		transport = TSSLTransportFactory.getServerSocket(port, RemoteSyncConfig.getTimeout(), null, params);

		if ("simple".equals(type)) {
			server = new TSimpleServer(new TSimpleServer.Args(transport).processor(processor));
		} else if ("mult_thread".equals(type)) {
			server = new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor));
		} else {
			throw new RuntimeException("can not create secure server by type:" + type);
		}

		// Use this for a multi threaded server
		// TServer server = new TThreadPoolServer(new
		// TThreadPoolServer.Args(serverTransport).processor(processor));

		logger.info("Starting the secure server...");
		return server;
	}

	public RemoteFileManage get(String folder) {
		return this.folders.get(folder).get();
	}

}
