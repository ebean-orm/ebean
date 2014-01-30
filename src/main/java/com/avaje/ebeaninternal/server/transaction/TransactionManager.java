package com.avaje.ebeaninternal.server.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.TransactionEventListener;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool;

/**
 * Manages transactions.
 * <p>
 * Keeps the Cache and Cluster in synch when transactions are committed.
 * </p>
 */
public class TransactionManager {

	private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
	
  public static final Logger SQL_LOGGER = LoggerFactory.getLogger("org.avaje.ebean.SQL");
  
  public static final Logger SUM_LOGGER = LoggerFactory.getLogger("org.avaje.ebean.SUM");
  
  public static final Logger TXN_LOGGER = LoggerFactory.getLogger("org.avaje.ebean.TXN");
  
	/**
	 * The behaviour desired when ending a query only transaction.
	 */
	public enum OnQueryOnly {
		
		/**
		 * Rollback the transaction.
		 */
		ROLLBACK,
		
		/**
		 * Just close the transaction.
		 */
		CLOSE_ON_READCOMMITTED,
		
		/**
		 * Commit the transaction
		 */
		COMMIT
	}
    
	private final BeanDescriptorManager beanDescriptorManager;
	
	/**
	 * Prefix for transaction id's (logging).
	 */
	private final String prefix;

	private final String externalTransPrefix;

	/**
	 * The dataSource of connections.
	 */
	private final DataSource dataSource;

	/**
	 * Flag to indicate the default Isolation is READ COMMITTED. This enables us
	 * to close queryOnly transactions rather than commit or rollback them.
	 */
	private final OnQueryOnly onQueryOnly;

	/**
	 * The default batchMode for transactions.
	 */
	private final boolean defaultBatchMode;

	private final BackgroundExecutor backgroundExecutor;
			
	private final ClusterManager clusterManager;
	
	//private final int commitDebugLevel;

	private final String serverName;
	
	/**
	 * Id's for transaction logging.
	 */
	private AtomicLong transactionCounter = new AtomicLong(1000);
	
	private int clusterDebugLevel;
	
	private final BulkEventListenerMap bulkEventListenerMap;

    private TransactionEventListener[] transactionEventListeners;

	/**
	 * Create the TransactionManager
	 */
	public TransactionManager(ClusterManager clusterManager, BackgroundExecutor backgroundExecutor, ServerConfig config, 
	      BeanDescriptorManager descMgr, BootupClasses bootupClasses) {
		
		this.beanDescriptorManager = descMgr;
		this.clusterManager = clusterManager;
		this.serverName = config.getName();		
		this.backgroundExecutor = backgroundExecutor;		
		this.dataSource = config.getDataSource();
		this.bulkEventListenerMap = new BulkEventListenerMap(config.getBulkTableEventListeners());

    List<TransactionEventListener> transactionEventListeners = bootupClasses.getTransactionEventListeners();
    this.transactionEventListeners = transactionEventListeners.toArray(new TransactionEventListener[transactionEventListeners.size()]);
		
    // log some transaction events using a java util logger
    this.clusterDebugLevel = GlobalProperties.getInt("ebean.cluster.debuglevel", 0);
		
		this.defaultBatchMode = config.isPersistBatching();
		
		this.prefix = GlobalProperties.get("transaction.prefix", "");
		this.externalTransPrefix = GlobalProperties.get("transaction.prefix", "e");
		
		String value = GlobalProperties.get("transaction.onqueryonly", "ROLLBACK").toUpperCase().trim();
		this.onQueryOnly = getOnQueryOnly(value, dataSource);
		
		initialiseHeartbeat();
	}
	
	private void initialiseHeartbeat() {
	  if (dataSource instanceof DataSourcePool) {
	    DataSourcePool ds = (DataSourcePool)dataSource;
	    backgroundExecutor.executePeriodically(ds.getHeartbeatRunnable(), ds.getHeartbeatFreqSecs(), TimeUnit.SECONDS);
	  }
	}
	
	public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
	  if (shutdownDataSource && (dataSource instanceof DataSourcePool)) {
	    ((DataSourcePool)dataSource).shutdown(deregisterDriver);
	  }	  
	}
	
	public BeanDescriptorManager getBeanDescriptorManager() {
        return beanDescriptorManager;
    }

	public BulkEventListenerMap getBulkEventListenerMap() {
    	return bulkEventListenerMap;
    }
	
	/**
	 * Return the behaviour to use when a query only transaction is committed.
	 * <p>
	 * There is a potential optimisation available when read committed is the default 
	 * isolation level. If it is, then Connections used only for queries do not require 
	 * commit or rollback but instead can just be put back into the pool via close().
	 * </p>
	 * <p>
	 * If the Isolation level is higher (say SERIALIZABLE) then Connections used
	 * just for queries do need to be committed or rollback after the query.
	 * </p>
	 */
	private OnQueryOnly getOnQueryOnly(String onQueryOnly, DataSource ds) {
		
		
		if (onQueryOnly.equals("COMMIT")){
			return OnQueryOnly.COMMIT;
		}
		if (onQueryOnly.startsWith("CLOSE")){
			if (!isReadCommitedIsolation(ds)){
				String m = "transaction.queryonlyclose is true but the transaction Isolation Level is not READ_COMMITTED";
				throw new PersistenceException(m);
			} else {
				return OnQueryOnly.CLOSE_ON_READCOMMITTED;				
			}
		}
		// default to rollback
		return OnQueryOnly.ROLLBACK;
	}		
	
	/**
	 * Return true if the isolation level is read committed.
	 */
	private boolean isReadCommitedIsolation(DataSource ds) {
		
		Connection c = null;
		try {
			c = ds.getConnection();

			int isolationLevel = c.getTransactionIsolation();
			return (isolationLevel == Connection.TRANSACTION_READ_COMMITTED);

		} catch (SQLException ex) {
			String m = "Errored trying to determine the default Isolation Level";
			throw new PersistenceException(m, ex);			

		} finally {
			try {
				if (c != null) {
					c.close();
				}
			} catch (SQLException ex) {
				logger.error("closing connection", ex);
			}
		}
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Return the cluster debug level.
	 */
	public int getClusterDebugLevel() {
        return clusterDebugLevel;
    }

    /**
     * Set the cluster debug level. 
     */
    public void setClusterDebugLevel(int clusterDebugLevel) {
        this.clusterDebugLevel = clusterDebugLevel;
    }

    /**
	 * Defines the type of behaviour to use when closing a transaction that was used to query data only.
	 */
	public OnQueryOnly getOnQueryOnly() {
		return onQueryOnly;
	}

	/**
	 * Wrap the externally supplied Connection.
	 */
	public SpiTransaction wrapExternalConnection(Connection c) {

		return wrapExternalConnection(externalTransPrefix + c.hashCode(), c);
	}
	
	/**
	 * Wrap an externally supplied Connection with a known transaction id.
	 */
	public SpiTransaction wrapExternalConnection(String id, Connection c) {

		ExternalJdbcTransaction t = new ExternalJdbcTransaction(id, true, c, this);

		// set the default batch mode. This can be on for
		// jdbc drivers that support getGeneratedKeys
		if (defaultBatchMode){
			t.setBatchMode(true);
		}
				
		return t;
	}
	
	/**
	 * Create a new Transaction.
	 */
	public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
	  Connection c = null;
		try {
		  c = dataSource.getConnection();
		  long id = transactionCounter.incrementAndGet();

			JdbcTransaction t = new JdbcTransaction(prefix + id, explicit, c, this);

			// set the default batch mode. This can be on for
			// jdbc drivers that support getGeneratedKeys
			if (defaultBatchMode){
				t.setBatchMode(true);
			}
			if (isolationLevel > -1) {
				c.setTransactionIsolation(isolationLevel);
			}
			
			if (explicit && TXN_LOGGER.isTraceEnabled()) {
			  TXN_LOGGER.trace(t.getLogPrefix()+"Begin");
			}
			
			return t;

		} catch (SQLException ex) {
		  // close connection on failed creation
		  try {
		    if (c != null){
		      c.close();
		    }
		  } catch (SQLException e) {
		    logger.error("Error closing failed connection", e);
		  }
			throw new PersistenceException(ex);
		}
	}
	
	public SpiTransaction createQueryTransaction() {
	  Connection c = null;
		try {
      c = dataSource.getConnection();
		  long id = transactionCounter.incrementAndGet();

			JdbcTransaction t = new JdbcTransaction(prefix + id, false, c, this);
			
			// set the default batch mode. Can be true for
			// jdbc drivers that support getGeneratedKeys
			if (defaultBatchMode){
				t.setBatchMode(true);
			}
			
			return t;
			
		} catch (PersistenceException ex) {
		  // close the connection and re-throw the exception
      try {
        if (c != null) {
          c.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing failed connection", e);
      }
      throw ex;
      
		} catch (SQLException ex) {
		  // don't need to close connection in this case 
			throw new PersistenceException(ex);
		}
	}

	/**
	 * Process a local rolled back transaction.
	 */
	public void notifyOfRollback(SpiTransaction transaction, Throwable cause) {
		
		try {
		  if (TXN_LOGGER.isInfoEnabled()) {
		    String msg = transaction.getLogPrefix()+"Rollback";
        if (cause != null){
          msg += " error: "+formatThrowable(cause);
        }
        TXN_LOGGER.info(msg);
      }
		 
		  for (TransactionEventListener listener : transactionEventListeners) {
        listener.postTransactionRollback(transaction, cause);
      }

		} catch (Exception ex) {
			logger.error("Error while notifying TransactionEventListener of rollback event", ex);
		}
	}

  /**
   * Query only transaction in read committed isolation.
   */
  public void notifyOfQueryOnly(boolean onCommit, SpiTransaction transaction, Throwable cause) {

    // Nothing that interesting here
    if (TXN_LOGGER.isTraceEnabled()) {
      TXN_LOGGER.trace(transaction.getLogPrefix()+"Commit - query only");
    }
  }
		
	private String formatThrowable(Throwable e){
		if (e == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		formatThrowable(e, sb);
		return sb.toString();
	}
	
	private void formatThrowable(Throwable e, StringBuilder sb){
		
		sb.append(e.toString());
		StackTraceElement[] stackTrace = e.getStackTrace();
		if (stackTrace.length > 0){
			sb.append(" stack0: ");
			sb.append(stackTrace[0]);
		}
		Throwable cause = e.getCause();
		if (cause != null){
			sb.append(" cause: ");
			formatThrowable(cause, sb);
		}
	}
	
	/**
	 * Process a local committed transaction.
	 */
	public void notifyOfCommit(SpiTransaction transaction) {

		try {

      if (transaction.isExplicit()) {
        if (TXN_LOGGER.isInfoEnabled()) {
          TXN_LOGGER.info(transaction.getLogPrefix()+"Commit");
        }
      } else if (TXN_LOGGER.isDebugEnabled()) {
        TXN_LOGGER.debug(transaction.getLogPrefix()+"Commit");        
      }
      
      PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, transaction, transaction.getEvent());

      postCommit.notifyLocalCacheIndex();
      postCommit.notifyCluster();

      // cluster and text indexing
      backgroundExecutor.execute(postCommit.notifyPersistListeners());

      for (TransactionEventListener listener : transactionEventListeners) {
        listener.postTransactionCommit(transaction);
      }
      
		} catch (Exception ex) {
			String m = "NotifyOfCommit failed. L2 Cache potentially not notified.";
			logger.error(m, ex);
		}
	}
	



	/**
	 * Process a Transaction that comes from another framework or local code.
	 * <p>
	 * For cases where raw SQL/JDBC or other frameworks are used this can
	 * invalidate the appropriate parts of the cache.
	 * </p>
	 */
	public void externalModification(TransactionEventTable tableEvents) {
				
		TransactionEvent event = new TransactionEvent();
		event.add(tableEvents);
		
		PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, null, event);
        
		// invalidate parts of local cache and index
		postCommit.notifyLocalCacheIndex();
		
		backgroundExecutor.execute(postCommit.notifyPersistListeners());
	}
	
	
    /**
     * Notify local BeanPersistListeners etc of events from another server in the cluster.
     */
	public void remoteTransactionEvent(RemoteTransactionEvent remoteEvent) {
        
        if (clusterDebugLevel > 0 || logger.isDebugEnabled()){
            logger.info("Cluster Received: "+remoteEvent.toString());
        }

        List<TableIUD> tableIUDList = remoteEvent.getTableIUDList();
        if (tableIUDList != null){
            for (int i = 0; i < tableIUDList.size(); i++) {
                TableIUD tableIUD = tableIUDList.get(i);
                beanDescriptorManager.cacheNotify(tableIUD);
            }
        }
        
        List<BeanPersistIds> beanPersistList = remoteEvent.getBeanPersistList();
        if (beanPersistList != null){
            for (int i = 0; i < beanPersistList.size(); i++) {
                BeanPersistIds beanPersist = beanPersistList.get(i);
                beanPersist.notifyCacheAndListener();
            }
        }
        
    }
	

}
