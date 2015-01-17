package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform.OnQueryOnly;
import com.avaje.ebean.event.TransactionEventListener;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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

	protected final BeanDescriptorManager beanDescriptorManager;
	
	/**
	 * Prefix for transaction id's (logging).
	 */
	protected final String prefix;

	protected final String externalTransPrefix;

	/**
	 * The dataSource of connections.
	 */
	protected final DataSource dataSource;

	/**
	 * Flag to indicate the default Isolation is READ COMMITTED. This enables us
	 * to close queryOnly transactions rather than commit or rollback them.
	 */
	protected final OnQueryOnly onQueryOnly;

	protected final BackgroundExecutor backgroundExecutor;
			
	protected final ClusterManager clusterManager;
	
	protected final String serverName;

  protected final PersistBatch persistBatch;

  protected final PersistBatch persistBatchOnCascade;

	/**
	 * Id's for transaction logging.
	 */
	protected final AtomicLong transactionCounter = new AtomicLong(1000);

	protected final BulkEventListenerMap bulkEventListenerMap;

	protected final TransactionEventListener[] transactionEventListeners;

	/**
	 * Create the TransactionManager
	 */
	public TransactionManager(ClusterManager clusterManager, BackgroundExecutor backgroundExecutor, ServerConfig config, 
	      BeanDescriptorManager descMgr, BootupClasses bootupClasses) {

    this.persistBatch = config.getPersistBatch();
    this.persistBatchOnCascade = config.appliedPersistBatchOnCascade();
		this.beanDescriptorManager = descMgr;
		this.clusterManager = clusterManager;
		this.serverName = config.getName();		
		this.backgroundExecutor = backgroundExecutor;		
		this.dataSource = config.getDataSource();
		this.bulkEventListenerMap = new BulkEventListenerMap(config.getBulkTableEventListeners());

    List<TransactionEventListener> transactionEventListeners = bootupClasses.getTransactionEventListeners();
    this.transactionEventListeners = transactionEventListeners.toArray(new TransactionEventListener[transactionEventListeners.size()]);

		this.prefix = "";
		this.externalTransPrefix = "e";
		
		this.onQueryOnly = initOnQueryOnly(config.getDatabasePlatform().getOnQueryOnly(), dataSource);
		
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

  public PersistBatch getPersistBatch() {
    return persistBatch;
  }

  public PersistBatch getPersistBatchOnCascade() {
    return persistBatchOnCascade;
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
	private OnQueryOnly initOnQueryOnly(OnQueryOnly dbPlatformOnQueryOnly, DataSource ds) {

		// first check for a system property 'override'
		String systemPropertyValue = System.getProperty("ebean.transaction.onqueryonly");
		if (systemPropertyValue != null) {
			return OnQueryOnly.valueOf(systemPropertyValue.trim().toUpperCase());
		}

		if (OnQueryOnly.CLOSE.equals(dbPlatformOnQueryOnly)) {
			// check for read committed isolation level
			if (!isReadCommitedIsolation(ds)){
				logger.warn("Ignoring DatabasePlatform.OnQueryOnly.CLOSE as the transaction Isolation Level is not READ_COMMITTED");
				// we will just use ROLLBACK and ignore the desired optimisation
				return OnQueryOnly.ROLLBACK;
			} else {
				// will use the OnQueryOnly.CLOSE optimisation
				return OnQueryOnly.CLOSE;
			}
		}
		// default to rollback if not defined on the platform
		return dbPlatformOnQueryOnly == null ? OnQueryOnly.ROLLBACK : dbPlatformOnQueryOnly;
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
   * Defines the type of behavior to use when closing a transaction that was used to query data only.
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

		// set the default batch mode
    t.setBatch(persistBatch);
    t.setBatchOnCascade(persistBatchOnCascade);
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

			SpiTransaction t = createTransaction(explicit, c, id);
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

			return createTransaction(false, c, id);

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
	 * Create a new transaction.
	 */
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {
    return new JdbcTransaction(prefix + id, explicit, c, this);
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
      
      PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, transaction.getEvent());

      postCommit.notifyLocalCacheIndex();
      postCommit.notifyCluster();

      // cluster and text indexing
      backgroundExecutor.execute(postCommit.notifyPersistListeners());

      for (TransactionEventListener listener : transactionEventListeners) {
        listener.postTransactionCommit(transaction);
      }

    } catch (Exception ex) {
			logger.error("NotifyOfCommit failed. L2 Cache potentially not notified.", ex);
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
		
		PostCommitProcessing postCommit = new PostCommitProcessing(clusterManager, this, event);
        
		// invalidate parts of local cache and index
		postCommit.notifyLocalCacheIndex();
		
		backgroundExecutor.execute(postCommit.notifyPersistListeners());
	}

  /**
   * Notify local BeanPersistListeners etc of events from another server in the cluster.
   */
  public void remoteTransactionEvent(RemoteTransactionEvent remoteEvent) {

    if (logger.isDebugEnabled()) {
      logger.debug("Cluster Received: " + remoteEvent.toString());
    }

    List<TableIUD> tableIUDList = remoteEvent.getTableIUDList();
    if (tableIUDList != null) {
      for (int i = 0; i < tableIUDList.size(); i++) {
        TableIUD tableIUD = tableIUDList.get(i);
        beanDescriptorManager.cacheNotify(tableIUD);
      }
    }

    List<BeanPersistIds> beanPersistList = remoteEvent.getBeanPersistList();
    if (beanPersistList != null) {
      for (int i = 0; i < beanPersistList.size(); i++) {
        BeanPersistIds beanPersist = beanPersistList.get(i);
        beanPersist.notifyCacheAndListener();
      }
    }
  }

}
