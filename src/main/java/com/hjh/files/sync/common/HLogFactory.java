package com.hjh.files.sync.common;

public class HLogFactory {

	private static ILogFactory instance;

	public static boolean isInstanceNull() {
		return null == instance;
	}

	private static ILog empty = new ILog() {

		@Override
		public void debug(String msg) {
		}

		@Override
		public void info(String msg) {
		}

		@Override
		public void error(String msg, Throwable e) {
		}

		@Override
		public void stdout(String msg) {
		}

	};

	public static ILog create(Class<?> type) {
		if (null == instance) {
			return empty;
		}
		return instance.create(type);
	}

	public static void setInstance(ILogFactory instance) {
		HLogFactory.instance = instance;
	}

}
