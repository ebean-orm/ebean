package com.avaje.ebeaninternal.server.jmx;

import com.avaje.ebean.AdminLogging;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.LogLevel;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;

/**
 * Implementation of the LogControl.
 * <p>
 * This is accessible via {@link EbeanServer#getAdminLogging()} or via JMX MBean.
 * </p>
 */
public class MAdminLogging implements MAdminLoggingMBean, AdminLogging {

	private final TransactionManager transactionManager;

	private boolean debugSql;
	private boolean debugLazyLoad;
	
	/**
	 * Configure from serverConfig properties.
	 */
	public MAdminLogging(ServerConfig serverConfig, TransactionManager txManager) {

		this.transactionManager = txManager;
		this.debugSql = serverConfig.isDebugSql();
		this.debugLazyLoad = serverConfig.isDebugLazyLoad();
	}

	public void setLogLevel(LogLevel logLevel){
		transactionManager.setTransactionLogLevel(logLevel);
	}
	
	public LogLevel getLogLevel() {
		return transactionManager.getTransactionLogLevel();
	}

	public boolean isDebugGeneratedSql() {
		return debugSql;
	}

	public void setDebugGeneratedSql(boolean debugSql) {
		this.debugSql = debugSql;
	}

	public boolean isDebugLazyLoad() {
		return debugLazyLoad;
	}

	public void setDebugLazyLoad(boolean debugLazyLoad) {
		this.debugLazyLoad = debugLazyLoad;
	}

}
