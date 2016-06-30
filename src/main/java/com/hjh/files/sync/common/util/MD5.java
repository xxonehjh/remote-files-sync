package com.hjh.files.sync.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	
	public static String md5(File file)throws IOException{
		
		InputStream input = new FileInputStream(file);
		try{
			return md5(input);
		}finally{
			input.close();
		}
		
	}

	public static String md5(InputStream input) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] cache = new byte[1024];
			int len;
			while ((len = input.read(cache)) >= 0) {
				md.update(cache, 0, len);
			}
			cache = null;
			return toString(md);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} finally {
			input.close();
		}
	}

	public static String md5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			return toString(md);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String toString(MessageDigest md) {
		byte b[] = md.digest();
		int i;
		StringBuffer buf = new StringBuffer("");
		for (int offset = 0; offset < b.length; offset++) {
			i = b[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				buf.append("0");
			buf.append(Integer.toHexString(i));
		}
		return buf.toString();
	}

}
