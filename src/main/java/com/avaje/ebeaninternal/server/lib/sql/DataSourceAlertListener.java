package com.avaje.ebeaninternal.server.lib.sql;


/**
 * Listens for alerting events such as DataSource down.
 */
public interface DataSourceAlertListener {

	/**
	 * Send an Alert saying the dataSource is down.
	 */
	public void dataSourceDown(String dataSourceName);
	
	/**
	 * Send an Alert saying the dataSource is back up.
	 */
	public void dataSourceUp(String dataSourceName);
	
	/**
	 * Send an Alert saying the dataSource has reached a high 
	 * number of connections.
	 */
	public void warning(String subject, String msg);
	

}
