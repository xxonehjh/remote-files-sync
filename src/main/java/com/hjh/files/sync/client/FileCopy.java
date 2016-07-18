package com.hjh.files.sync.client;

import java.io.File;
import java.io.IOException;

import com.hjh.files.sync.common.RemoteFile;
import com.hjh.files.sync.common.StopAble;

public interface FileCopy {

	public void copy(StopAble stop, RemoteFile from, File target, String md5) throws IOException;
}
