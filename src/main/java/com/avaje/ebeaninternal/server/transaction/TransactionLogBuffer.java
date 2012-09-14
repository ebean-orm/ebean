package com.avaje.ebeaninternal.server.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffer of transaction messages.
 * <p>
 * For performance reasons we add all the transaction messages to an instance of
 * TransactionLogBuffer and then when the buffer is full or the transaction ends
 * we send the buffer to the transaction manager to log.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class TransactionLogBuffer {

    private final String transactionId;
    
    private final ArrayList<LogEntry> buffer;

    private final int maxSize;

    private int currentSize;

    /**
     * Create the buffer with a maxSize and transaction id.
     */
    public TransactionLogBuffer(int maxSize, String transactionId) {
        this.maxSize = maxSize;
        this.transactionId = transactionId;
        this.buffer = new ArrayList<LogEntry>(maxSize);
    }

    /**
     * Create new buffer using the same configuration.
     */
    public TransactionLogBuffer newBuffer() {
        return new TransactionLogBuffer(maxSize, transactionId);
    }

    /**
     * Return the transaction id.
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Add a message to the buffer.
     */
    public boolean add(String msg) {
        buffer.add(new LogEntry(msg));
        return ++currentSize >= maxSize;
    }
    
    /**
     * Return true if the buffer is empty.
     */
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    /**
     * Return all the messages.
     */
    public List<LogEntry> messages() {
        return buffer;
    }

    /**
     * Entry in the buffer.
     */
    public class LogEntry {

        private final long timestamp;
        private final String msg;

        /**
         * Construct an entry.
         */
        public LogEntry(String msg) {
            this.timestamp = System.currentTimeMillis();
            this.msg = msg;
        }

        /**
         * Return the time the entry was put into the buffer.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Return the text of the message.
         */
        public String getMsg() {
            return msg;
        }
    }
}
