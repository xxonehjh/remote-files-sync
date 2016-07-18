package com.hjh.files.sync.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.Asserts;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.StopAble;
import com.hjh.files.sync.common.util.MD5;
import com.hjh.files.sync.common.util.RemoteFileUtil;

public class FileCopyBySimple implements FileCopy {
	private static ILog logger = HLogFactory.create(FileCopyBySimple.class);

	private ClientFolder client_folder;
	private int block_size;

	public FileCopyBySimple(ClientFolder client_folder, String store_folder, int block_size) {
		this.client_folder = client_folder;
		this.block_size = block_size;
	}

	@Override
	public void copy(StopAble stop, RemoteFile from, File target, String md5) throws IOException {

		Asserts.check(!target.exists(), "file already exist:" + target.getAbsolutePath());

		target.createNewFile();
		int totalParts = RemoteFileUtil.countPart(from.length(), this.block_size);
		FileOutputStream out = new FileOutputStream(target);
		try {
			for (int i = 0; i < totalParts; i++) {
				byte[] part_data = client_folder.getFromManage().part(from.path(), i, block_size);
				out.write(part_data);
				logger.debug(String.format("[%s] [%s] [%d/%d] receive part data %d K", this.client_folder.getName(),
						from.path(), i + 1, totalParts, part_data.length / 1024));
			}
		} finally {
			out.close();
			out = null;
		}

		String target_md5 = MD5.md5(target);
		if (!md5.equals(target_md5)) {
			logger.stdout("clear dirty file : " + target.getAbsolutePath());
			Asserts.check(target.delete(), "can not clear dirty file:" + target.getAbsolutePath());
			throw new RuntimeException(
					"can not fetch correct data from remote for:" + from.path() + ":" + target_md5 + ":" + md5);
		}

	}

}
