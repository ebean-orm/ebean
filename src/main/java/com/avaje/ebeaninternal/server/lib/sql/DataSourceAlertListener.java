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
