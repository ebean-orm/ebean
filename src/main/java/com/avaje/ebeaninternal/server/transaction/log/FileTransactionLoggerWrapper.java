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
package com.avaje.ebeaninternal.server.transaction.log;

import java.util.logging.Logger;

import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer;
import com.avaje.ebeaninternal.server.transaction.TransactionLogWriter;

/**
 * Wraps a FileTransactionLogger to provide delayed initialisation.
 * <p>
 * This means that it should only create the log file WHEN there is something
 * actually logged. This means the logLevel can start at NONE and then change at
 * runtime (and the log file will then be initialised). NOTE that a volatile
 * with double checked locking is used to make this transition thread safe.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class FileTransactionLoggerWrapper implements TransactionLogWriter {

    private static final Logger logger = Logger.getLogger(FileTransactionLoggerWrapper.class.getName());

    private final String serverName;
    private final String dir;
    private final int maxFileSize;

    private volatile FileTransactionLogger logWriter;

    public FileTransactionLoggerWrapper(ServerConfig serverConfig) {

        String evalDir = serverConfig.getLoggingDirectoryWithEval();
        this.dir = evalDir != null ? evalDir : "logs";
        this.maxFileSize = GlobalProperties.getInt("ebean.logging.maxFileSize", 100 * 1024 * 1024);
        this.serverName = serverConfig.getName();
    }

    private FileTransactionLogger initialiseLogger() {

        synchronized (this) {
            // double check locking here so logWriter NEEDS to be volatile!!
            FileTransactionLogger writer = this.logWriter;
            if (writer != null) {
                return writer;
            }

            String middleName = GlobalProperties.get("ebean.logging.filename", "_txn_");
            String logPrefix = serverName + middleName;
            String threadName = "Ebean-" + serverName + "-TxnLogWriter";

            // create the real logger and start it
            FileTransactionLogger newLogWriter = new FileTransactionLogger(threadName, dir, logPrefix, maxFileSize);

            // assignment of volatile field
            this.logWriter = newLogWriter;

            // start background thread for the writer
            newLogWriter.start();
            logger.info("Transaction logs in: " + dir);
            return newLogWriter;
        }
    }

    public void log(TransactionLogBuffer logBuffer) {
        // volatile read
        FileTransactionLogger writer = this.logWriter;
        if (writer == null) {
            writer = initialiseLogger();
        }
        writer.log(logBuffer);
    }

    public void shutdown() {
        if (logWriter != null) {
            logWriter.shutdown();
        }

    }

}
