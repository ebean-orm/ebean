/**
 * Copyright (C) 2009 the original author or authors
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
package com.avaje.ebean.springsupport.txn;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.avaje.ebean.config.ExternalTransactionManager;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.transaction.DefaultTransactionThreadLocal;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;

/**
 * A Spring aware TransactionScopeManager.
 * 
 * <p>
 * Will look for Spring transactions and use them if they exist.
 * </p>
 * 
 * @since 18.05.2009
 * @author E Mc Greal
 */
public class SpringAwareJdbcTransactionManager implements ExternalTransactionManager {

    private final static Logger logger = Logger.getLogger(SpringAwareJdbcTransactionManager.class.getName());

    /** 
     * The data source. 
     */
    private DataSource dataSource;

    /** 
     * The Ebean transaction manager. 
     */
    private TransactionManager transactionManager;

    /**
     *  The EbeanServer name. 
     */
    private String serverName;
    
    /**
     * Instantiates a new spring aware transaction scope manager.
     */
    public SpringAwareJdbcTransactionManager() {
    }

    /**
     * Initialise this with the Ebean internal transaction manager.
     */
    public void setTransactionManager(Object txnMgr) {
        
        // RB: At this stage not exposing TransactionManager to 
        // the public API and hence the Object type and casting here
        
        this.transactionManager = (TransactionManager) txnMgr;
        this.dataSource = transactionManager.getDataSource();
        this.serverName = transactionManager.getServerName();
    }

    /**
     * Looks for a current Spring managed transaction and wraps/returns that as a Ebean transaction.
     * <p>
     * Returns null if there is no current spring transaction (lazy loading outside a spring txn etc).
     * </p>
     */
    public Object getCurrentTransaction() {

        // Get the current Spring ConnectionHolder associated to the current spring managed transaction
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);

        if (holder == null || !holder.isSynchronizedWithTransaction()) {
            // no current Spring transaction
            SpiTransaction currentEbeanTransaction = DefaultTransactionThreadLocal.get(serverName);
            if (currentEbeanTransaction != null){
                // NOT expecting this so log WARNING
                String msg = "SpringTransaction - no current spring txn BUT using current Ebean one "+currentEbeanTransaction.getId();
                logger.log(Level.WARNING, msg);
                
            } else if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Spring Txn - no current transaction ");
            }
            return currentEbeanTransaction;
        }
        
        SpringTxnListener springTxnLister = getSpringTxnListener();
        
        if (springTxnLister != null){
            // we have already seen this transaction 
            return springTxnLister.getTransaction();
            
        } else {
            // This is a new spring transaction that we have not seen before.
            // "wrap" it in a SpringJdbcTransaction for use with Ebean 
            SpringJdbcTransaction newTrans = new SpringJdbcTransaction(holder, transactionManager);
            
            // Create and register a Spring TransactionSynchronization for this transaction
            springTxnLister = createSpringTxnListener(newTrans);
            TransactionSynchronizationManager.registerSynchronization(springTxnLister);
            
            // also put in Ebean ThreadLocal
            DefaultTransactionThreadLocal.set(serverName, newTrans);
            return newTrans;
        }
    }

    /**
     * Search for our specific transaction listener.
     * <p>
     * If it exists then we have already seen and "wrapped" this transaction.
     * </p>
     */
    private SpringTxnListener getSpringTxnListener() {

        if (TransactionSynchronizationManager.isSynchronizationActive()){
	        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
	        if (synchronizations != null){
	            // search for our specific listener
	            for (int i = 0; i < synchronizations.size(); i++) {
	                if (synchronizations.get(i) instanceof SpringTxnListener){
	                    return (SpringTxnListener)synchronizations.get(i);
	                }
	            }
	        }
        }
        
        return null;
    }
    
    /**
     * Create a listener to register with Spring to enable Ebean to be 
     * notified when transactions commit and rollback.
     * <p>
     * This is used by Ebean to notify it's appropriate listeners and maintain it's server
     * cache etc.
     * </p>
     */
    private SpringTxnListener createSpringTxnListener(SpringJdbcTransaction t) {
        return new SpringTxnListener(transactionManager, t);
    }

    /**
     * A Spring TransactionSynchronization that we register with Spring to get
     * notified when a Spring managed transaction has been committed or rolled
     * back.
     * <p>
     * When Ebean is notified (of the commit/rollback) it can then manage its
     * cache, notify BeanPersistListeners etc.
     * </p>
     */
    private static class SpringTxnListener extends TransactionSynchronizationAdapter {
        
        private final TransactionManager transactionManager;
        
        private final SpringJdbcTransaction transaction;
        
        private final String serverName;
        
        private SpringTxnListener(TransactionManager transactionManager, SpringJdbcTransaction t){
            this.transactionManager = transactionManager;
            this.transaction = t;
            this.serverName = transactionManager.getServerName();
        }
        
        /**
         * Return the associated Ebean wrapped transaction.
         */
        public SpringJdbcTransaction getTransaction() {
            return transaction;
        }
        
        @Override
        public void beforeCommit(boolean readOnly) {
            // Future note: for JPA2 locking we will
            // have beforeCommit events to fire
        }

        @Override
        public void afterCompletion(int status) {
            
            switch (status) {
            case STATUS_COMMITTED:
                if (logger.isLoggable(Level.FINE)){
                    logger.fine("Spring Txn ["+transaction.getId()+"] committed");                    
                }
                transactionManager.notifyOfCommit(transaction);
                break;
                
            case STATUS_ROLLED_BACK:
                if (logger.isLoggable(Level.FINE)){
                    logger.fine("Spring Txn ["+transaction.getId()+"] rollback");                    
                }
                transactionManager.notifyOfRollback(transaction, null);
                break;
                
            default:
                // this should never happen
                String msg = "Invalid status "+status;
                throw new PersistenceException(msg);
            }
            
            // Remove this transaction object as it is completed
    		DefaultTransactionThreadLocal.replace(serverName, null);
        }
    }
}
