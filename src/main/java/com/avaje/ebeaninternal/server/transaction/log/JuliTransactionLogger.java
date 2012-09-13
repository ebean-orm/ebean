/**
 * Copyright (C) 2009  Authors
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
package com.avaje.ebeaninternal.server.transaction.log;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer;
import com.avaje.ebeaninternal.server.transaction.TransactionLogWriter;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer.LogEntry;

/**
 * A transactionLogger that uses a java.util.logging.Logger.
 * <p>
 * See {@link ServerConfig#setUseJuliTransactionLogger(boolean)}
 * </p>
 * @author rbygrave
 */
public class JuliTransactionLogger implements TransactionLogWriter {

	private static Logger logger = Logger.getLogger(JuliTransactionLogger.class.getName());
	
	public void log(TransactionLogBuffer logBuffer) {
     
	    String txnId = logBuffer.getTransactionId();
	    
	    List<LogEntry> messages = logBuffer.messages();
	    for (int i = 0; i < messages.size(); i++) {
	        LogEntry logEntry = messages.get(i);
	        log(txnId, logEntry);
        }
    }

    public void shutdown() {
    }

    private void log(String txnId, LogEntry entry) {
		
        String message = entry.getMsg();
		if (txnId != null && message != null && !message.startsWith("Trans[")){
			message = "Trans["+txnId+"] "+message;
		}
		
		logger.log(Level.INFO, message);
	}

	
}
