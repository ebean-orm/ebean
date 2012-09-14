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
