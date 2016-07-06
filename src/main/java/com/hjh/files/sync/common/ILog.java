package com.hjh.files.sync.common;

public abstract class ILog {

	public abstract void debug(String msg);

	public abstract void info(String msg);

	public abstract void error(String msg, Throwable e);
	
	public abstract void stdout(String msg);

}
