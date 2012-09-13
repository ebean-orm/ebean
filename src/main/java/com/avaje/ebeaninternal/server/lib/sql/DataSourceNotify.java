package com.avaje.ebeaninternal.server.lib.sql;

/**
 * Listener for notifications about the DataSource such as when the DataSource
 * goes down, up or gets close to it's maximum size.
 * <p>
 * The intention is to send email notifications to an administrator (or similar)
 * when these events occur on the DataSource.
 * </p>
 */
public interface DataSourceNotify {

	/**
	 * Send an alert to say the dataSource is back up.
	 */
	public void notifyDataSourceUp(String dataSourceName);

	/**
	 * Send an alert to say the dataSource is down.
	 */
	public void notifyDataSourceDown(String dataSourceName);

	/**
	 * Send an alert to say the dataSource is getting close to its max size.
	 */
	public void notifyWarning(String subject, String msg);
}
