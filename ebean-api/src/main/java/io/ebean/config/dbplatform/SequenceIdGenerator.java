package io.ebean.config.dbplatform;

import io.avaje.applog.AppLog;
import io.ebean.BackgroundExecutor;
import io.ebean.Transaction;
import io.ebean.util.JdbcClose;

import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

/**
 * Database sequence based IdGenerator.
 * <p>
 * Maintains a separate buffer of pre-fetched id values per tenant when the supplied
 * DataSource implements {@link TenantConnectionSource}. For the common single-tenant
 * case a single buffer is used (keyed by {@link #SINGLE}).
 */
public abstract class SequenceIdGenerator implements PlatformIdGenerator {

  protected static final System.Logger log = AppLog.getLogger("io.ebean.SEQ");

  /**
   * Buffer key used when there is no current tenant (single-tenant or no tenant in scope).
   */
  private static final Object SINGLE = new Object();

  protected final String seqName;
  protected final DataSource dataSource;
  protected final BackgroundExecutor backgroundExecutor;
  protected final int allocationSize;
  private final TenantConnectionSource tenantSource;
  private final TenantBuffer single = new TenantBuffer();
  private final ConcurrentMap<Object, TenantBuffer> buffers = new ConcurrentHashMap<>();

  /**
   * Per-tenant pre-fetched id buffer with its own lock and background-loading flag.
   */
  private static final class TenantBuffer {
    final ReentrantLock lock = new ReentrantLock();
    final NavigableSet<Long> idList = new TreeSet<>();
    final AtomicBoolean currentlyBackgroundLoading = new AtomicBoolean(false);
  }

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  protected SequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int allocationSize) {
    this.backgroundExecutor = be;
    this.dataSource = ds;
    this.seqName = seqName;
    this.allocationSize = allocationSize;
    this.tenantSource = (ds instanceof TenantConnectionSource) ? (TenantConnectionSource) ds : null;
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

  private Object currentTenantKey() {
    if (tenantSource != null) {
      Object tenantId = tenantSource.currentTenantId();
      if (tenantId != null) {
        return tenantId;
      }
    }
    return SINGLE;
  }

  private TenantBuffer buffer(Object tenantKey) {
    if (tenantKey == SINGLE) {
      // common single-tenant path - avoid the concurrent map lookup
      return single;
    }
    return buffers.computeIfAbsent(tenantKey, k -> new TenantBuffer());
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
   */
  @Override
  public Object nextId(Transaction t) {
    Object tenantKey = currentTenantKey();
    TenantBuffer buffer = buffer(tenantKey);
    buffer.lock.lock();
    try {
      int size = buffer.idList.size();
      if (size > 0) {
        maybeLoadMoreInBackground(size);
      } else {
        loadMore(tenantKey, buffer, allocationSize);
      }
      return buffer.idList.pollFirst();
    } finally {
      buffer.lock.unlock();
    }
  }

  private void maybeLoadMoreInBackground(int currentSize) {
    if (allocationSize > 1) {
      if (currentSize <= allocationSize / 2) {
        loadInBackground(allocationSize);
      }
    }
  }

  private void loadMore(Object tenantKey, TenantBuffer buffer, int requestSize) {
    List<Long> newIds = getMoreIds(tenantKey, requestSize);
    buffer.lock.lock();
    try {
      buffer.idList.addAll(newIds);
    } finally {
      buffer.lock.unlock();
    }
  }

  /**
   * Load another batch of Id's using a background thread.
   * <p>
   * The tenant is captured here (submit time) as the current tenant is not in scope
   * on the background executor thread.
   */
  protected void loadInBackground(final int requestSize) {
    final Object tenantKey = currentTenantKey();
    final TenantBuffer buffer = buffer(tenantKey);
    if (!buffer.currentlyBackgroundLoading.compareAndSet(false, true)) {
      // skip as already background loading
      log.log(DEBUG, "... skip background sequence load (another load in progress)");
      return;
    }
    backgroundExecutor.execute(() -> {
      try {
        loadMore(tenantKey, buffer, requestSize);
      } finally {
        buffer.currentlyBackgroundLoading.set(false);
      }
    });
  }

  /**
   * Read the resultSet returning the list of Id values.
   */
  protected abstract List<Long> readIds(ResultSet resultSet, int loadSize) throws SQLException;

  /**
   * Get more Id's by executing a query and reading the Id's returned.
   */
  protected List<Long> getMoreIds(Object tenantKey, int requestSize) {

    String sql = getSql(requestSize);

    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = connectionFor(tenantKey);

      statement = connection.prepareStatement(sql);
      resultSet = statement.executeQuery();

      List<Long> newIds = readIds(resultSet, requestSize);
      if (newIds.isEmpty()) {
        throw new PersistenceException("Always expecting more than 1 row from " + sql);
      }
      connection.commit();
      return newIds;

    } catch (SQLException e) {
      if (e.getMessage().contains("Database is already closed")) {
        String msg = "Error getting SEQ when DB shutting down " + e.getMessage();
        log.log(ERROR, msg);
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
   * Return a connection for the given tenant. For multi-tenant this is routed to the
   * tenant database/schema/catalog; otherwise the plain DataSource connection is used.
   */
  private Connection connectionFor(Object tenantKey) throws SQLException {
    if (tenantSource != null && tenantKey != SINGLE) {
      return tenantSource.connectionForTenant(tenantKey);
    }
    return dataSource.getConnection();
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
