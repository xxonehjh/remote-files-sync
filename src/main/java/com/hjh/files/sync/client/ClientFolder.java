package com.hjh.files.sync.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.RemoteFileFactory;
import com.hjh.files.sync.common.RemoteFileManage;
import com.hjh.files.sync.common.RemoteSyncConfig;
import com.hjh.files.sync.common.StopAble;
import com.hjh.files.sync.common.util.MD5;

public class ClientFolder {

	private static ILog logger = HLogFactory.create(ClientFolder.class);

	private RemoteFileManage fromManage;

	private String store_folder;
	private String name;
	private String url;
	private FileCopy fileCopy;

	public ClientFolder(String name, String store_folder, String url, int block_size) {
		this.name = name;
		this.store_folder = store_folder;
		this.url = url;
		this.fileCopy = new FileCopyByCache(store_folder, block_size);
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

	public String getStore_folder() {
		return store_folder;
	}

	public void setStore_folder(String store_folder) {
		this.store_folder = store_folder;
	}

	public void sync(StopAble stop) throws IOException {

		String store_path = new File(new File(store_folder), name).getCanonicalPath();
		if (null == fromManage) {
			fromManage = RemoteFileFactory.queryManage(url);
		}
		logger.stdout(String.format("sync[%s] %s => %s", name, url, store_path));
		File root = new File(store_path);
		if (!root.exists()) {
			root.mkdir();
		}
		Asserts.check(root.isDirectory(), "must be a directory :" + store_path);
		long time = System.currentTimeMillis();
		try {
			if (stop.isStop()) {
				return;
			}
			doSync(stop, null, root);
		} finally {
			long end = System.currentTimeMillis();
			logger.stdout(String.format("sync finish[%s](cost: %s) %s => %s", name, (end - time) / 1000 + "s", url,
					store_path));
		}
	}

	private void doSync(StopAble stop, RemoteFile from, File target) throws IOException {

		if (null == from || from.isFolder()) { // 目录同步
			String path = null == from ? null : from.path();
			if (stop.isStop()) {
				return;
			}
			RemoteFile[] remotes = fromManage.list(path);
			if (stop.isStop()) {
				return;
			}
			if (target.isFile()) {
				logger.stdout("remove file:" + target.getAbsolutePath());
				Asserts.check(target.delete(), "delete file fail : " + target.getAbsolutePath());
			}
			if (!target.exists()) {
				logger.stdout(String.format("sync folder[%s] %s => %s", name, path, target.getAbsolutePath()));
				Asserts.check(target.mkdir(), "create folder fail : " + target.getAbsolutePath());
			}
			String[] exists = target.list();
			for (RemoteFile item : remotes) {
				doSync(stop, item, new File(target, item.name()));
				if (null != exists) {
					for (int i = 0; i < exists.length; i++) {
						if (exists[i] != null && exists[i].equals(item.name())) {
							exists[i] = null;
							break;
						}
					}
				}
			}
			if (null != exists) {
				for (int i = 0; i < exists.length; i++) {
					if (exists[i] != null) {
						File cur_exist = new File(target, exists[i]);
						if (cur_exist.isDirectory()) {
							logger.stdout("remove directory:" + cur_exist.getAbsolutePath());
							FileUtils.deleteDirectory(cur_exist);
						} else {
							logger.stdout("remove file:" + cur_exist.getAbsolutePath());
							Asserts.check(cur_exist.delete(), "can not delete file :" + cur_exist.getAbsolutePath());
						}
					}
				}
			}
		} else { // 文件同步
			if (!isSame(from, target)) {
				logger.stdout(String.format("sync file[%s] %s => %s", name, from.path(), target.getAbsolutePath()));
				if (stop.isStop()) {
					return;
				}
				String md5 = fromManage.md5(from.path());
				if (stop.isStop()) {
					return;
				}
				if (target.isDirectory()) {
					logger.stdout("remove directory:" + target.getAbsolutePath());
					FileUtils.deleteDirectory(target);
				}

				String local_md5 = target.isFile() ? MD5.md5(target) : null;
				if (!md5.equals(local_md5)) {
					if (target.isFile()) {
						logger.stdout("remove unmatch file:" + target.getAbsolutePath());
						Asserts.check(target.delete(),
								String.format("can not delete file : %s", target.getAbsolutePath()));
					}
					fileCopy.copy(stop, from, target, md5);
				}
				target.setLastModified(from.lastModify());
			}
		}
	}

	private boolean isSame(RemoteFile from, File to) {

		if (!to.exists()) {
			return false;
		}

		if (from.isFolder()) {
			if (!to.isDirectory()) {
				return false;
			}
		} else {
			if (to.isDirectory()) {
				return false;
			}
		}

		if (!from.name().equals(to.getName())) {
			return false;
		}

		if (from.lastModify() != to.lastModified()) {
			if (Math.abs(from.lastModify() - to.lastModified()) > RemoteSyncConfig.getMinDiffTime()) {
				logger.debug(String.format("[%s] %d <> %d", from.path(), from.lastModify(), to.lastModified()));
				return false;
			}
		}

		if (from.length() != to.length()) {
			return false;
		}
		return true;
	}

	public void validate() throws IOException {
		String store_path = new File(new File(store_folder), name).getCanonicalPath();
		if (null == fromManage) {
			fromManage = RemoteFileFactory.queryManage(url);
		}
		logger.stdout(String.format("validate [%s] %s => %s", name, url, store_path));
		File root = new File(store_path);
		if (!root.exists()) {
			logger.info("not any files in local");
			return;
		}
		long time = System.currentTimeMillis();
		try {
			doValidate(null, root);
		} finally {
			long end = System.currentTimeMillis();
			logger.stdout(String.format("validate finish[%s](cost: %s) %s => %s", name, (end - time) / 1000 + "s", url,
					store_path));
		}
	}

	private void doValidate(RemoteFile from, File target) throws IOException {
		if (null == from || from.isFolder()) { // 目录同步
			String path = null == from ? null : from.path();
			RemoteFile[] remotes = fromManage.list(path);
			if (target.isFile()) {
				logger.stdout("file type error (must be a folder) :" + target.getAbsolutePath());
			} else if (!target.exists()) {
				logger.stdout("can not found folder:" + target.getAbsolutePath());
			} else {
				String[] exists = target.list();
				for (RemoteFile item : remotes) {
					doValidate(item, new File(target, item.name()));
					if (null != exists) {
						for (int i = 0; i < exists.length; i++) {
							if (exists[i] != null && exists[i].equals(item.name())) {
								exists[i] = null;
								break;
							}
						}
					}
				}
				if (null != exists) {
					for (int i = 0; i < exists.length; i++) {
						if (exists[i] != null) {
							File cur_exist = new File(target, exists[i]);
							logger.stdout("must remove:" + cur_exist.getAbsolutePath());
						}
					}
				}
			}
		} else {
			if (!target.exists()) {
				logger.stdout("can not found file:" + target.getAbsolutePath());
			} else if (target.isDirectory()) {
				logger.stdout("file type error (must be a file) :" + target.getAbsolutePath());
			} else {
				if (!isSame(from, target)) {
					logger.stdout("file info not match : " + target.getAbsolutePath());
				}
				String md5_from = fromManage.md5(from.path());
				String md5_target = MD5.md5(target);
				if (!md5_from.equals(md5_target)) {
					logger.stdout("file md5 not match : " + target.getAbsolutePath());
				}
			}
		}
	}

	public RemoteFileManage getFromManage() {
		return this.fromManage;
	}

}
