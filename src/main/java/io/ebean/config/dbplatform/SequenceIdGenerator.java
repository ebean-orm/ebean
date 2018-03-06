package io.ebean.config.dbplatform;

import io.ebean.BackgroundExecutor;
import io.ebean.Transaction;
import io.ebean.util.JdbcClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Database sequence based IdGenerator.
 */
public abstract class SequenceIdGenerator implements PlatformIdGenerator {

  protected static final Logger logger = LoggerFactory.getLogger("io.ebean.SEQ");

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

  protected final ArrayList<Long> idList = new ArrayList<>(50);

  protected final int allocationSize;

  protected boolean currentlyBackgroundLoading;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  protected SequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int allocationSize) {
    this.backgroundExecutor = be;
    this.dataSource = ds;
    this.seqName = seqName;
    this.allocationSize = allocationSize;
  }

  public abstract String getSql(int batchSize);

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
   * If allocateSize is large load some sequences in a background thread.
   * <p>
   * For example, when inserting a bean with a cascade on a OneToMany with many
   * beans Ebean can call this to ensure .
   * </p>
   */
  @Override
  public void preAllocateIds(int requestSize) {
    // do nothing by default
  }

  /**
   * Return the next Id.
   * <p>
   * If a Transaction has been passed in use the Connection from it.
   * </p>
   */
  @Override
  public Object nextId(Transaction t) {
    synchronized (monitor) {

      int size = idList.size();
      if (size > 0) {
        maybeLoadMoreInBackground(size);
      } else {
        loadMore(allocationSize);
      }

      return idList.remove(0);
    }
  }

  private void maybeLoadMoreInBackground(int currentSize) {
    if (allocationSize > 1) {
      if (currentSize <= allocationSize / 2) {
        loadInBackground(allocationSize);
      }
    }
  }

  private void loadMore(int requestSize) {
    List<Long> newIds = getMoreIds(requestSize);
    synchronized (monitor) {
      idList.addAll(newIds);
    }
  }

  /**
   * Load another batch of Id's using a background thread.
   */
  protected void loadInBackground(final int requestSize) {

    // single threaded processing...
    synchronized (backgroundLoadMonitor) {
      if (currentlyBackgroundLoading) {
        // skip as already background loading
        logger.debug("... skip background sequence load (another load in progress)");
        return;
      }

      currentlyBackgroundLoading = true;

      backgroundExecutor.execute(() -> {
        loadMore(requestSize);
        synchronized (backgroundLoadMonitor) {
          currentlyBackgroundLoading = false;
        }
      });
    }
  }

  /**
   * Read the resultSet returning the list of Id values.
   */
  protected abstract List<Long> readIds(ResultSet resultSet, int loadSize) throws SQLException;

  /**
   * Get more Id's by executing a query and reading the Id's returned.
   */
  protected List<Long> getMoreIds(int requestSize) {

    String sql = getSql(requestSize);

    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = dataSource.getConnection();

      statement = connection.prepareStatement(sql);
      resultSet = statement.executeQuery();

      List<Long> newIds = readIds(resultSet, requestSize);
      if (logger.isTraceEnabled()) {
        logger.trace("seq:{} loaded:{} sql:{}", seqName, newIds.size(), sql);
      }
      if (newIds.isEmpty()) {
        throw new PersistenceException("Always expecting more than 1 row from " + sql);
      }

      return newIds;

    } catch (SQLException e) {
      if (e.getMessage().contains("Database is already closed")) {
        String msg = "Error getting SEQ when DB shutting down " + e.getMessage();
        logger.error(msg);
        System.out.println(msg);
        return Collections.emptyList();
      } else {
        throw new PersistenceException("Error getting sequence nextval", e);
      }
    } finally {
      closeResources(connection, statement, resultSet);
    }
  }

  /**
   * Close the JDBC resources.
   */
  private void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
    JdbcClose.close(resultSet);
    JdbcClose.close(statement);
    JdbcClose.close(connection);
  }

}
