package io.ebean.config.dbplatform;

import io.ebean.BackgroundExecutor;
import io.ebean.Transaction;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Database sequence based IdGenerator.
 */
public abstract class SequenceIdGenerator implements PlatformIdGenerator {

  private static final Logger logger = LoggerFactory.getLogger(SequenceIdGenerator.class);

  /**
   * The actual sequence name.
   */
  protected final String seqName;

  protected final TenantDataSourceProvider dataSourceProvider;

  protected final BackgroundExecutor backgroundExecutor;

  protected final int batchSize;
  
  private final ConcurrentMap<Object, IdLoader> idLoaders;
  
  private final IdLoader idLoader;
  
  private final CurrentTenantProvider currentTenantProvider;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public SequenceIdGenerator(BackgroundExecutor be, TenantDataSourceProvider ds, String seqName, int batchSize, 
      CurrentTenantProvider currentTenantProvider) {
    this.backgroundExecutor = be;
    this.dataSourceProvider = ds;
    this.seqName = seqName;
    this.batchSize = batchSize;
    this.currentTenantProvider = currentTenantProvider;
    if (currentTenantProvider != null) {
      idLoader = null;
      idLoaders = new ConcurrentHashMap<>();
    } else {
      idLoader = new IdLoader(dataSourceProvider.dataSource(null));
      idLoaders = null;
    }
  }

  public abstract String getSql(int batchSize);

  /**
   * Converts the resultset into IDs
   */
  protected List<Long> processResult(ResultSet rset, int loadSize) throws SQLException {
    List<Long> newIds = new ArrayList<>(loadSize);
    while (rset.next()) {
      newIds.add(rset.getLong(1));
   }
   return newIds;
  }

  /**
   * Returns the sequence name.
   */
  @Override
  public String getName() {
    return seqName;
  }

  /**
   * Returns true.
   */
  @Override
  public boolean isDbSequence() {
    return true;
  }

  /**
   * A per tenant instance for loading beans
   * @author Roland Praml, FOCONIS AG
   *
   */
  protected class IdLoader {
    
    /**
     * Used to synchronise the idList access.
     */
    protected final Object monitor = new Object();
    
    /**
     * Used to synchronise background loading (loadBatchInBackground).
     */
    protected final Object backgroundLoadMonitor = new Object();
    
    protected final ArrayList<Long> idList = new ArrayList<>(50);

    protected final Object tenantId;
    
    protected final DataSource dataSource;
    
    protected int currentlyBackgroundLoading;
    
    protected IdLoader(DataSource ds) {
      tenantId = null;
      this.dataSource = ds;
    }
    
    protected IdLoader(Object tenantId, DataSource ds) {
      this.tenantId = tenantId;
      this.dataSource = ds;
    }
    
    public Object nextId(Transaction t) {
      synchronized (monitor) {

        if (idList.isEmpty()) {
          loadMoreIds(batchSize, t);
        }
        Long nextId = idList.remove(0);

        if (batchSize > 1) {
          if (idList.size() <= batchSize / 2) {
            loadBatchInBackground();
          }
        }

        return nextId;
      }
      
    }
    
    /**
     * Load another batch of Id's using a background thread.
     */
    protected void loadBatchInBackground() {

      // single threaded processing...
      synchronized (backgroundLoadMonitor) {

        if (currentlyBackgroundLoading > 0) {
          // skip as already background loading
          logger.debug("... skip background sequence load (another load in progress)");
          return;
        }

        currentlyBackgroundLoading = batchSize;

        backgroundExecutor.execute(() -> {
          loadMoreIds(batchSize, null);
          synchronized (backgroundLoadMonitor) {
            currentlyBackgroundLoading = 0;
          }
        });
      }
    }
    
    protected void loadMoreIds(final int numberToLoad, Transaction t) {

      List<Long> newIds = getMoreIds(numberToLoad, t);

      if (logger.isDebugEnabled()) {
        logger.debug("... seq: {} loaded: {} tenant: {} ids: {}",seqName,numberToLoad, tenantId, newIds);
      }

      synchronized (monitor) {
        idList.addAll(newIds);
      }
    }
    

    /**
     * Get more Id's by executing a query and reading the Id's returned.
     */
    protected List<Long> getMoreIds(int loadSize, Transaction t) {

      String sql = getSql(loadSize);
      

      boolean useTxnConnection = t != null;

      Connection c = null;
      Statement pstmt = null;
      ResultSet rset = null;
      try {
        c = useTxnConnection ? t.getConnection() : dataSource.getConnection();

        pstmt = c.createStatement();
        rset = pstmt.executeQuery(sql);

        List<Long> newIds = processResult(rset, loadSize);
        if (newIds.isEmpty()) {
          throw new PersistenceException("Always expecting more than 1 row from " + sql);
        }
        return newIds;
      } catch (SQLException e) {
        if (e.getMessage().contains("Database is already closed")) {
          String msg = "Error getting SEQ when DB shutting down " + e.getMessage();
          logger.info(msg);
          System.out.println(msg);
          return new ArrayList<>();
        } else {
          throw new PersistenceException("Error getting sequence nextval", e);
        }
      } finally {
        if (useTxnConnection) {
          closeResources(null, pstmt, rset);
        } else {
          closeResources(c, pstmt, rset);
        }
      }
    }

    /**
     * Close the JDBC resources.
     */
    protected void closeResources(Connection c, Statement pstmt, ResultSet rset) {
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing ResultSet", e);
      }
      try {
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing PreparedStatement", e);
      }
      try {
        if (c != null) {
          c.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing Connection", e);
      }
    }
  }
  
  /**
   * Returns the IdLoader for this requestContext.
   * @param requestContext
   * @return
   */
  protected IdLoader getIdLoader(Object tenantId, DataSource ds) {
    if (idLoader != null) {
      return idLoader;
    } else {
      return idLoaders.computeIfAbsent(tenantId, id ->  new IdLoader(id, ds));
    }
  }
  /**
   * If allocateSize is large load some sequences in a background thread.
   * <p>
   * For example, when inserting a bean with a cascade on a OneToMany with many
   * beans Ebean can call this to ensure .
   * </p>
   */
  @Override
  public void preAllocateIds(int allocateSize) {
    if (batchSize > 1 && allocateSize > batchSize) {
      // only bother if allocateSize is bigger than
      // the normal loading batchSize
      if (allocateSize > 100) {
        // max out at 100 for now
        allocateSize = 100;
      }
      loadLargeAllocation(allocateSize);
    }
  }

  /**
   * Called by preAllocateIds when we know that a large number of Id's is going
   * to be needed shortly.
   */
  protected void loadLargeAllocation( final int allocateSize) {
    // preAllocateIds was called with a relatively large batchSize
    // so we will just go ahead and load those anyway in background
    final Object tenantId = currentTenantProvider == null ? null : currentTenantProvider.currentId(); // read ID of this thread
    DataSource ds = dataSourceProvider.dataSource(tenantId); // we MUST fetch the DS outside the BE-Thread
    backgroundExecutor.execute(() -> getIdLoader(tenantId, ds).loadMoreIds(allocateSize, null));
  }

  /**
   * Return the next Id.
   * <p>
   * If a Transaction has been passed in use the Connection from it.
   * </p>
   */
  @Override
  public Object nextId(Transaction t) {
    final Object tenantId = currentTenantProvider == null ? null : currentTenantProvider.currentId();
    DataSource ds = dataSourceProvider.dataSource(tenantId);
    return getIdLoader(tenantId, ds).nextId(t);
  }

}
