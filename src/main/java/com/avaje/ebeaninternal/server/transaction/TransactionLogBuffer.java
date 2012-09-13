/**
 * Copyright (C) 2009 Authors
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
