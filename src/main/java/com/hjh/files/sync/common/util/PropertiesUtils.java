package com.hjh.files.sync.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.util.Asserts;

public class PropertiesUtils {

	public static Properties load(String path) throws IOException {
		Properties p = new Properties();
		if (new File(path).exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(new File(path));
				p.load(in);
			} finally {
				if (null != in) {
					in.close();
				}
			}
		} else {
			InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream(path);
			Asserts.check(null!=in, "can not load resource :" + path);
			p.load(in);
		}
		return p;
	}

}
