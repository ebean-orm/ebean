package com.avaje.ebeaninternal.server.jmx;

import com.avaje.ebean.LogLevel;

public interface MAdminLoggingMBean {

	/**
	 * The current log level .
	 */
	public LogLevel getLogLevel();

	/**
	 * Set the log level for native sql queries.
	 */
	public void setLogLevel(LogLevel logLevel);

	/**
	 * If true Log generated sql to the console.
	 */
	public boolean isDebugGeneratedSql();

	/**
	 * Set to true to Log generated sql to the console.
	 */
	public void setDebugGeneratedSql(boolean debugSql);

	/**
	 * Return true if lazy loading should be debugged.
	 */
	public boolean isDebugLazyLoad();

	/**
	 * Set the debugging on lazy loading.
	 */
	public void setDebugLazyLoad(boolean debugLazyLoad);

}