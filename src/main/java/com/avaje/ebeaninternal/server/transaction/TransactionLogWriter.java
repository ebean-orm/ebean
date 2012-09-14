package com.avaje.ebeaninternal.server.transaction;

/**
 * Write transaction log events to a file or other destination.
 */
public interface TransactionLogWriter {

    /**
     * Log all the messages in the buffer.
     */
    public void log(TransactionLogBuffer logBuffer);

    /**
     * Shutdown the writer.
     */
    public void shutdown();

}
