package com.hjh.files.sync.common.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteFileManage;
import com.hjh.files.sync.common.util.RemoteFileUtil;

import tutorial.SyncFileServer;

public class RemoteFileManageThriftImpl implements RemoteFileManage {

	private String folder;
	private String ip;
	private int port;
	private String truststore;
	private TTransport transport;
	private SyncFileServer.Client client;

	private void initTransport() throws TTransportException {
		if (null == truststore) {
			transport = new TSocket(ip, port);
			transport.open();
		} else {
			/*
			 * Similar to the server, you can use the parameters to setup client
			 * parameters or use the default settings. On the client side, you
			 * will need a TrustStore which contains the trusted certificate
			 * along with the public key. For this example it's a self-signed
			 * cert.
			 */
			String truststore_arr[] = truststore.split("@");
			TSSLTransportParameters params = new TSSLTransportParameters();
			params.setTrustStore(truststore_arr[0], truststore_arr[1], "SunX509", "JKS");
			/*
			 * Get a client transport instead of a server transport. The
			 * connection is opened on invocation of the factory method, no need
			 * to specifically call open()
			 */
			transport = TSSLTransportFactory.getClientSocket(ip, port, 0, params);
		}

		TProtocol protocol = new TBinaryProtocol(transport);
		client = new SyncFileServer.Client(protocol);
	}

	public RemoteFileManageThriftImpl(String ip, int port, String folder, String truststore)
			throws TTransportException {

		this.ip = ip;
		this.port = port;
		this.truststore = truststore;
		this.folder = folder;

		initTransport();
	}

	public RemoteFile[] list(String parent) {
		try {
			client.ping();
			return RemoteFileUtil.from(client.listFiles(folder, parent));
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	public String md5(String file) {
		try {
			return client.md5(folder, file);
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	public int partCount(long len) {
		try {
			return client.partCount(folder, len);
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] part(String file, int part) {
		try {
			return client.part(folder, file, part).array();
		} catch (TException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		if (null != this.transport) {
			transport.close();
		}
	}

}
