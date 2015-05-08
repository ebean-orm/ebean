package com.avaje.ebeaninternal.server.transaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.ExternalTransactionManager;
import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * Hook into external JTA transaction manager.
 * 
 * @author rbygrave
 */
public class JtaTransactionManager implements ExternalTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(JtaTransactionManager.class);

    private static final String EBEAN_TXN_RESOURCE = "EBEAN_TXN_RESOURCE";
    
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
    public JtaTransactionManager() {
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

    private TransactionSynchronizationRegistry getSyncRegistry() {
        try {
            InitialContext ctx = new InitialContext();
            return (TransactionSynchronizationRegistry)ctx.lookup("java:comp/TransactionSynchronizationRegistry");
        } catch (NamingException e){
            throw new PersistenceException(e);
        }
    }
    
    private UserTransaction getUserTransaction() {
        try {
            InitialContext ctx = new InitialContext();
            return (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        } catch (NamingException e){
            // assuming CMT
        	return new DummyUserTransaction();
        }
    }
    
    /**
     * Looks for a current Spring managed transaction and wraps/returns that as a Ebean transaction.
     * <p>
     * Returns null if there is no current spring transaction (lazy loading outside a spring txn etc).
     * </p>
     */
    public Object getCurrentTransaction() {

        TransactionSynchronizationRegistry syncRegistry = getSyncRegistry();
        
        SpiTransaction t = (SpiTransaction)syncRegistry.getResource(EBEAN_TXN_RESOURCE);
        if (t != null){
            // we have already seen this transaction 
            return t;
        }
        
        // check current Ebean transaction
        SpiTransaction currentEbeanTransaction = DefaultTransactionThreadLocal.get(serverName);
        if (currentEbeanTransaction != null){
            // NOT expecting this so log WARNING
            String msg = "JTA Transaction - no current txn BUT using current Ebean one "+currentEbeanTransaction.getId();
            logger.warn(msg);
            return currentEbeanTransaction;
        } 
        
        UserTransaction ut = getUserTransaction();
        if (ut == null){
            // no current JTA transaction
            if (logger.isDebugEnabled()){
                logger.debug("JTA Transaction - no current txn");
            }
            return null;
        }
        
        // This is a transaction that Ebean has not seen before.
    
        // "wrap" it in a Ebean specific JtaTransaction
        String txnId = String.valueOf(System.currentTimeMillis());
        JtaTransaction newTrans = new JtaTransaction(txnId, true, ut, dataSource, transactionManager);

        // create and register transaction listener
        JtaTxnListener txnListener = createJtaTxnListener(newTrans);
        
        syncRegistry.putResource(EBEAN_TXN_RESOURCE, newTrans);
        syncRegistry.registerInterposedSynchronization(txnListener);
        
        // also put in Ebean ThreadLocal
        DefaultTransactionThreadLocal.set(serverName, newTrans);
        return newTrans;
    }

    
    /**
     * Create a listener to register with JTA to enable Ebean to be 
     * notified when transactions commit and rollback.
     * <p>
     * This is used by Ebean to notify it's appropriate listeners and maintain it's server
     * cache etc.
     * </p>
     */
    private JtaTxnListener createJtaTxnListener(SpiTransaction t) {
        return new JtaTxnListener(transactionManager, t);
    }
    
    private static class DummyUserTransaction implements UserTransaction {

        public void begin() throws NotSupportedException, SystemException {
		}

		public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
				SecurityException, IllegalStateException, SystemException {
		}

		public int getStatus() throws SystemException {
			return 0;
		}

		public void rollback() throws IllegalStateException, SecurityException, SystemException {
		}

		public void setRollbackOnly() throws IllegalStateException, SystemException {
		}

		public void setTransactionTimeout(int seconds) throws SystemException {
		}
    }

    /**
     * A JTA Transaction Synchronization that we register to get notified when a
     * managed transaction has been committed or rolled back.
     * <p>
     * When Ebean is notified (of the commit/rollback) it can then manage its
     * cache, notify BeanPersistListeners etc.
     * </p>
     */
    private static class JtaTxnListener implements Synchronization {
        
        private final TransactionManager transactionManager;
        
        private final SpiTransaction transaction;
        
        private final String serverName;
        
        private JtaTxnListener(TransactionManager transactionManager, SpiTransaction t){
            this.transactionManager = transactionManager;
            this.transaction = t;
            this.serverName = transactionManager.getServerName();
        }

        public void beforeCompletion() {
          // Future note: for JPA2 locking we will
          // have beforeCommit events to fire
        }

        public void afterCompletion(int status) {
            
            switch (status) {
            case Status.STATUS_COMMITTED:
                if (logger.isDebugEnabled()){
                    logger.debug("Jta Txn ["+transaction.getId()+"] committed");
                }
                transactionManager.notifyOfCommit(transaction);
                // Remove this transaction object as it is completed
                DefaultTransactionThreadLocal.replace(serverName, null);
                break;
                
            case Status.STATUS_ROLLEDBACK:
                if (logger.isDebugEnabled()){
                    logger.debug("Jta Txn ["+transaction.getId()+"] rollback");
                }
                transactionManager.notifyOfRollback(transaction, null);
                // Remove this transaction object as it is completed
                DefaultTransactionThreadLocal.replace(serverName, null);
                break;
                
            default:
                logger.debug("Jta Txn ["+transaction.getId()+"] status:"+status);
            }
            
        }
    }
    
}
