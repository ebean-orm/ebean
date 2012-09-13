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

import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import com.avaje.ebean.LogLevel;

/**
 * Jta based transaction.
 */
public class JtaTransaction extends JdbcTransaction {

    private UserTransaction userTransaction;

    private DataSource dataSource;

    private boolean commmitted = false;

    private boolean newTransaction = false;


    /**
     * Create the JtaTransaction.
     */
    public JtaTransaction(String id, boolean explicit, LogLevel logLevel, UserTransaction utx, DataSource ds, TransactionManager manager) {
        super(id, explicit, logLevel, null, manager);
        userTransaction = utx;
        dataSource = ds;

        try {
            newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
            if (newTransaction) {
                userTransaction.begin();
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }

        try {
            // Open JDBC Connection
            this.connection = dataSource.getConnection();
            if (connection == null) {
                throw new PersistenceException("The DataSource returned a null connection.");
            }
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }

        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Commit the transaction.
     */
    public void commit() {
        if (commmitted) {
            throw new PersistenceException("This transaction has already been committed.");
        }
        try {
            try {
                if (newTransaction) {
                    userTransaction.commit();
                }
                notifyCommit();
            } finally {
                close();
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
        commmitted = true;
    }

    public void rollback() {
        rollback(null);
    }
    
    /**
     * Rollback the transaction.
     */
    public void rollback(Throwable e) {
        if (!commmitted) {
            try {
                try {
                    if (userTransaction != null) {
                        if (newTransaction) {
                            userTransaction.rollback();
                        } else {
                            userTransaction.setRollbackOnly();
                        }
                    }
                    notifyRollback(e);
                } finally {
                    close();
                }
            } catch (Exception ex) {
                throw new PersistenceException(ex);
            }
        }

    }

    /**
     * Close the underlying connection.
     */
    private void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

}
