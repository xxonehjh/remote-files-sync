package com.hjh.files.sync.common;

public class HLogFactory {

	private static ILogFactory instance;

	public static ILog create(Class<?> type) {
		return instance.create(type);
	}

	public static void setInstance(ILogFactory instance) {
		HLogFactory.instance = instance;
	}

}
