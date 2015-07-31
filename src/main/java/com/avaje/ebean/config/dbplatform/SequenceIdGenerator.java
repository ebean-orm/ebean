package com.avaje.ebean.config.dbplatform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database sequence based IdGenerator.
 */
public abstract class SequenceIdGenerator implements IdGenerator {

  private static final Logger logger = LoggerFactory.getLogger(SequenceIdGenerator.class);

  /**
   * Used to synchronise the idList access.
   */
  protected final Object monitor = new Object();

  /**
   * Used to synchronise background loading (loadBatchInBackground).
   */
  protected final Object backgroundLoadMonitor = new Object();

  /**
   * The actual sequence name.
   */
  protected final String seqName;

  protected final DataSource dataSource;

  protected final BackgroundExecutor backgroundExecutor;

  protected final ArrayList<Long> idList = new ArrayList<Long>(50);

  protected final int batchSize;

  protected int currentlyBackgroundLoading;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public SequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    this.backgroundExecutor = be;
    this.dataSource = ds;
    this.seqName = seqName;
    this.batchSize = batchSize;
  }

  public abstract String getSql(int batchSize);

  /**
   * Returns the sequence name.
   */
  public String getName() {
    return seqName;
  }

  /**
   * Returns true.
   */
  public boolean isDbSequence() {
    return true;
  }

  /**
   * If allocateSize is large load some sequences in a background thread.
   * <p>
   * For example, when inserting a bean with a cascade on a OneToMany with many
   * beans Ebean can call this to ensure .
   * </p>
   */
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
  protected void loadLargeAllocation(final int allocateSize) {
    // preAllocateIds was called with a relatively large batchSize
    // so we will just go ahead and load those anyway in background
    backgroundExecutor.execute(new Runnable() {
      public void run() {
        loadMoreIds(allocateSize, null);
      }
    });
  }

  /**
   * Return the next Id.
   * <p>
   * If a Transaction has been passed in use the Connection from it.
   * </p>
   */
  public Object nextId(Transaction t) {
    synchronized (monitor) {

      if (idList.size() == 0) {
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

      backgroundExecutor.execute(new Runnable() {
        public void run() {
          loadMoreIds(batchSize, null);
          synchronized (backgroundLoadMonitor) {
            currentlyBackgroundLoading = 0;
          }
        }
      });
    }
  }

  protected void loadMoreIds(final int numberToLoad, Transaction t) {

    ArrayList<Long> newIds = getMoreIds(numberToLoad, t);

    if (logger.isDebugEnabled()) {
      logger.debug("... seq:" + seqName + " loaded:" + numberToLoad + " ids:" + newIds);
    }

    synchronized (monitor) {
      for (int i = 0; i < newIds.size(); i++) {
        idList.add(newIds.get(i));
      }
    }
  }

  /**
   * Get more Id's by executing a query and reading the Id's returned.
   */
  protected ArrayList<Long> getMoreIds(int loadSize, Transaction t) {

    String sql = getSql(loadSize);

    ArrayList<Long> newIds = new ArrayList<Long>(loadSize);

    boolean useTxnConnection = t != null;

    Connection c = null;
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    try {
      c = useTxnConnection ? t.getConnection() : dataSource.getConnection();

      pstmt = c.prepareStatement(sql);
      rset = pstmt.executeQuery();
      while (rset.next()) {
        newIds.add(rset.getLong(1));
      }
      if (newIds.size() == 0) {
        throw new PersistenceException("Always expecting more than 1 row from " + sql);
      }

      return newIds;

    } catch (SQLException e) {
      if (e.getMessage().contains("Database is already closed")) {
        String msg = "Error getting SEQ when DB shutting down " + e.getMessage();
        logger.info(msg);
        System.out.println(msg);
        return newIds;
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
  protected void closeResources(Connection c, PreparedStatement pstmt, ResultSet rset) {
    try {
      if (rset != null) {
        rset.close();
      }
    } catch (SQLException e) {
      logger.error( "Error closing ResultSet", e);
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
