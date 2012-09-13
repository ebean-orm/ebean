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
package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.transaction.log.FileTransactionLoggerWrapper;
import com.avaje.ebeaninternal.server.transaction.log.JuliTransactionLogger;

/**
 * Manages the transaction logs.
 */
public class TransactionLogManager {

	private final TransactionLogWriter logWriter;

	/**
	 * Create the TransactionLogger.
	 * <p>
	 * DevNote: This registers a shutdown hook to flush and close the
	 * sharedLogger. Alternate option would be to flush() the log after each
	 * write to the log.
	 * </p>
	 */
	public TransactionLogManager(ServerConfig serverConfig) {

		if (serverConfig.isLoggingToJavaLogger()){
		    logWriter = new JuliTransactionLogger();		    
		} else {
		    logWriter = new FileTransactionLoggerWrapper(serverConfig);
		}
	}

	public void shutdown() {
	    logWriter.shutdown();
    }

    public void log(TransactionLogBuffer logBuffer) {
        logWriter.log(logBuffer);
    }
    
}
