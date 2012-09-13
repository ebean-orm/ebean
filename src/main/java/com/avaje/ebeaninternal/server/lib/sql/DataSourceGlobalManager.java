/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
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
