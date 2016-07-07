package com.hjh.files.sync.common.thrift;

import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;

import tutorial.SyncFileServer;

public class ThriftClientPool {

	private static ILog logger = HLogFactory.create(ThriftClientPool.class);

	static class ClientItem {
		public String ip;
		public int port;
		public String truststore;
		private TTransport transport;
		private SyncFileServer.Client client;

		public ClientItem(String ip, int port, String truststore) {
			logger.stdout("reg:" + ip + ":" + port + ":with:" + truststore);
			this.ip = ip;
			this.port = port;
			this.truststore = truststore;
		}

		public synchronized void close() {
			if (null != transport) {
				transport.close();
				transport = null;
				client = null;
			}
		}

		public SyncFileServer.Client get() {
			if (null != client) {
				try {
					client.ping();
				} catch (TException ex) {
					logger.error("client ping fail", ex);
					close();
				}
			}
			if (null == client) {
				try {
					init();
				} catch (TTransportException e) {
					throw new RuntimeException(e);
				}
			}
			return client;
		}

		public synchronized void init() throws TTransportException {
			if (null != client) {
				return;
			}
			if (null == truststore) {
				transport = new TSocket(ip, port);
				transport.open();
			} else {
				/*
				 * Similar to the server, you can use the parameters to setup
				 * client parameters or use the default settings. On the client
				 * side, you will need a TrustStore which contains the trusted
				 * certificate along with the public key. For this example it's
				 * a self-signed cert.
				 */
				String truststore_arr[] = truststore.split("@");
				TSSLTransportParameters params = new TSSLTransportParameters();
				if(truststore_arr[0].endsWith(".truststore")){
					params.setTrustStore(truststore_arr[0], truststore_arr[1], "SunX509", "JKS");
				}else{
					params.setTrustStore(truststore_arr[0], truststore_arr[1], "X509", "BKS");
				}
				/*
				 * Get a client transport instead of a server transport. The
				 * connection is opened on invocation of the factory method, no
				 * need to specifically call open()
				 */
				transport = TSSLTransportFactory.getClientSocket(ip, port, 0, params);
			}

			TProtocol protocol = new TBinaryProtocol(transport);
			client = new SyncFileServer.Client(protocol);
		}
	}

	private static Map<String, ClientItem> items = new HashMap<String, ClientItem>();

	public static synchronized void reg(String ip, int port, String truststore) {
		String key = ip + ":" + port;
		if (null == items.get(key)) {
			items.put(key, new ClientItem(ip, port, truststore));
		}
	}

	public static SyncFileServer.Client get(String ip, int port) {
		String key = ip + ":" + port;
		return items.get(key).get();
	}

	public static synchronized void closeAll() {
		for (ClientItem item : items.values()) {
			item.close();
		}
	}

}
