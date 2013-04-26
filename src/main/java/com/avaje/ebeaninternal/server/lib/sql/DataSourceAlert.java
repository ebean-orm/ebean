package com.avaje.ebeaninternal.server.lib.sql;

/**
 * Listener for notifications about the DataSource such as when the DataSource
 * goes down, up or gets close to it's maximum size.
 * <p>
 * The intention is to send email notifications to an administrator (or similar)
 * when these events occur on the DataSource.
 * </p>
 */
public interface DataSourceAlert {

	/**
	 * Send an alert to say the dataSource is back up.
	 */
	public void dataSourceUp(String dataSourceName);

	/**
	 * Send an alert to say the dataSource is down.
	 */
	public void dataSourceDown(String dataSourceName);

	/**
	 * Send an alert to say the dataSource is getting close to its max size.
	 */
	public void dataSourceWarning(String subject, String msg);
}
