package com.hjh.files.sync.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	private final static String CLIENT_CACHE_FOLDER_NAME = ".c.cache";

	private RemoteFileManage fromManage;

	private String store_folder;
	private String name;
	private String url;
	private File cache;

	public ClientFolder(String name, String store_folder, String url) {
		this.name = name;
		this.store_folder = store_folder;
		this.url = url;
		this.cache = new File(store_folder, CLIENT_CACHE_FOLDER_NAME);
		this.cache = new File(this.cache, "_" + RemoteSyncConfig.getBlockSize());
		if (!this.cache.isDirectory()) {
			Asserts.check(this.cache.mkdirs(),
					"can not create cache folder for client on :" + this.cache.getAbsolutePath());
		}

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
					File current_cache_root = new File(cache, md5.substring(0, 2));
					if (!current_cache_root.exists()) {
						current_cache_root.mkdir();
					}
					current_cache_root = new File(current_cache_root, md5);
					if (!current_cache_root.exists()) {
						current_cache_root.mkdir();
					}
					if (stop.isStop()) {
						return;
					}
					int totalParts = fromManage.partCount(from.length());
					if (stop.isStop()) {
						return;
					}
					int total_write_file_count = 0;
					int per_file_part_count = 5;
					for (int i = 0; i < totalParts; i++) {
						total_write_file_count++;
						File cur_part = new File(current_cache_root,
								total_write_file_count + "." + per_file_part_count);
						if (!cur_part.exists()) {
							File cur_part_temp = new File(current_cache_root,
									total_write_file_count + "." + System.currentTimeMillis() + ".temp");
							if (cur_part_temp.exists()) {
								Asserts.check(cur_part_temp.delete(),
										"can not delete :" + cur_part_temp.getAbsolutePath());
							}
							try {
								FileOutputStream out = new FileOutputStream(cur_part_temp);
								try {
									for (int j = 0; j < per_file_part_count; j++) {
										if (stop.isStop()) {
											return;
										}
										int cur_part_index = i + j;
										if (cur_part_index < totalParts) {
											byte[] part_data = fromManage.part(from.path(), cur_part_index);
											logger.debug(String.format("[%s] [%s] [%d/%d] receive part data %d K", name,
													from.path(), cur_part_index + 1, totalParts,
													part_data.length / 1024));
											out.write(part_data);
											part_data = null;
										}
										if (stop.isStop()) {
											return;
										}
									}
								} finally {
									out.close();
									out = null;
								}
								Asserts.check(cur_part_temp.renameTo(cur_part),
										"can not rename :" + cur_part_temp.getAbsolutePath());
							} finally {
								cur_part_temp.delete();
							}
						}
						i = i + per_file_part_count - 1;
					}

					File target_temp = new File(current_cache_root, "temp");
					if (target_temp.isDirectory()) {
						logger.stdout("remove directory:" + target_temp.getAbsolutePath());
						FileUtils.deleteDirectory(target_temp);
					}

					if (target_temp.isFile()) {
						String cache_md5 = MD5.md5(target_temp);
						if (!md5.equals(cache_md5)) {
							Asserts.check(target_temp.delete(),
									"can not delete wrong file[md5 do not match]:" + target_temp.getAbsolutePath());
						}
					}

					if (!target_temp.exists()) {
						target_temp.createNewFile();
						FileInputStream in = null;
						FileOutputStream out = new FileOutputStream(target_temp);
						byte[] cache = new byte[512];
						int len;
						try {
							for (int i = 1; i <= total_write_file_count; i++) {
								File cur_part = new File(current_cache_root, i + "." + per_file_part_count);
								in = new FileInputStream(cur_part);
								while (true) {
									len = in.read(cache);
									if (len > 0) {
										out.write(cache, 0, len);
									} else {
										break;
									}
								}
								in.close();
								in = null;
							}
						} finally {
							out.close();
							out = null;
							if (null != in) {
								in.close();
								in = null;
							}
							cache = null;
						}
					}

					{
						String cache_md5 = MD5.md5(target_temp);
						if (!md5.equals(cache_md5)) {
							logger.stdout("clear dirty directory : " + current_cache_root.getAbsolutePath());
							FileUtils.deleteDirectory(current_cache_root);
							throw new RuntimeException("can not fetch correct data from remote for:" + from.path() + ":"
									+ cache_md5 + ":" + md5);
						}
					}

					if (target.isFile()) {
						logger.stdout("remove unmatch file:" + target.getAbsolutePath());
						Asserts.check(target.delete(),
								String.format("can not delete file : %s", target.getAbsolutePath()));
					}

					Asserts.check(target_temp.renameTo(target), String.format("can not move file: %s => %s",
							target_temp.getAbsolutePath(), target.getAbsolutePath()));

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

}
