package com.avaje.ebeaninternal.server.lib.sql;

import java.util.List;

import com.avaje.ebean.config.DataSourceConfig;

/**
 * Manages access to named DataSources using singleton scope.
 */
public final class DataSourceGlobalManager {

	private static final DataSourceManager manager = new DataSourceManager();

	private DataSourceGlobalManager() {
	}

	/**
	 * Return true when the dataSource is shutting down.
	 */
	public static boolean isShuttingDown() {
		return manager.isShuttingDown();
	}

	/**
	 * Shutdown the dataSources.
	 */
	public static void shutdown() {
		manager.shutdown();
	}

	/**
	 * Return the list of DataSourcePool's.
	 */
	public static List<DataSourcePool> getPools() {
		return manager.getPools();
	}

	/**
	 * Return a DataSource pool by its name.
	 */
	public static DataSourcePool getDataSource(String name) {
		return manager.getDataSource(name);
	}
	
	public static DataSourcePool getDataSource(String name, DataSourceConfig dsConfig) {
		return manager.getDataSource(name, dsConfig);
	}
	

}
