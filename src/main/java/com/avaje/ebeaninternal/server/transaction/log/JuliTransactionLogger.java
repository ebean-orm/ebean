package com.avaje.ebeaninternal.server.transaction.log;

import java.util.List;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer.LogEntry;
import com.avaje.ebeaninternal.server.transaction.TransactionLogWriter;
import java.util.logging.Logger;

/**
 * A transactionLogger that uses a java.util.logging.Logger.
 * <p>
 * See {@link ServerConfig#setUseJuliTransactionLogger(boolean)}
 * </p>
 * @author rbygrave
 */
public class JuliTransactionLogger implements TransactionLogWriter {
  private static final Logger logger = Logger.getLogger(JuliTransactionLogger.class.getName());

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
		
		logger.info(message);
	}

	
}
