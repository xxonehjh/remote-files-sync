package com.hjh.files.sync.common.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hjh.files.sync.common.HLogFactory;
import com.hjh.files.sync.common.ILog;
import com.hjh.files.sync.common.ILogFactory;

public class LogUtil {

	public static void initLog() {
		if (!HLogFactory.isInstanceNull()) {
			return;
		}
		HLogFactory.setInstance(new ILogFactory() {

			public ILog create(Class<?> type) {

				final Log logger = LogFactory.getLog(type);

				return new ILog() {

					@Override
					public void debug(String msg) {
						logger.debug(msg);
					}

					@Override
					public void info(String msg) {
						logger.info(msg);
					}

					@Override
					public void error(String msg, Throwable e) {
						logger.error(msg, e);
					}

				};
			}

		});
	}

}
