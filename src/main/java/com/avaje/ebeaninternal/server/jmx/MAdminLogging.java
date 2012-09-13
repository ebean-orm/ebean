/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
