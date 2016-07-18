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
import com.hjh.files.sync.common.StopAble;
import com.hjh.files.sync.common.util.MD5;
import com.hjh.files.sync.common.util.RemoteFileUtil;

public class FileCopyByCache implements FileCopy {

	private final static String CLIENT_CACHE_FOLDER_NAME = ".c.cache";
	private static ILog logger = HLogFactory.create(FileCopyByCache.class);

	private ClientFolder client_folder;
	private File cache;
	private int block_size;

	public FileCopyByCache(ClientFolder client_folder, String store_folder, int block_size) {
		this.client_folder = client_folder;
		this.block_size = block_size;
		this.cache = new File(store_folder, CLIENT_CACHE_FOLDER_NAME);
		this.cache = new File(this.cache, "_" + block_size);
		if (!this.cache.isDirectory()) {
			Asserts.check(this.cache.mkdirs(),
					"can not create cache folder for client on :" + this.cache.getAbsolutePath());
		}
	}

	public void copy(StopAble stop, RemoteFile from, File target, String md5) throws IOException {

		Asserts.check(!target.exists(), "file already exist:" + target.getAbsolutePath());

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
		int totalParts = RemoteFileUtil.countPart(from.length(), this.block_size);
		if (stop.isStop()) {
			return;
		}
		int total_write_file_count = 0;
		int per_file_part_count = 5;
		for (int i = 0; i < totalParts; i++) {
			total_write_file_count++;
			File cur_part = new File(current_cache_root, total_write_file_count + "." + per_file_part_count);
			if (!cur_part.exists()) {
				File cur_part_temp = new File(current_cache_root,
						total_write_file_count + "." + System.currentTimeMillis() + ".temp");
				if (cur_part_temp.exists()) {
					Asserts.check(cur_part_temp.delete(), "can not delete :" + cur_part_temp.getAbsolutePath());
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
								byte[] part_data = client_folder.getFromManage().part(from.path(), cur_part_index,
										block_size);
								logger.debug(String.format("[%s] [%s] [%d/%d] receive part data %d K",
										this.client_folder.getName(), from.path(), cur_part_index + 1, totalParts,
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
				throw new RuntimeException(
						"can not fetch correct data from remote for:" + from.path() + ":" + cache_md5 + ":" + md5);
			}
		}

		Asserts.check(target_temp.renameTo(target),
				String.format("can not move file: %s => %s", target_temp.getAbsolutePath(), target.getAbsolutePath()));
	}

}
