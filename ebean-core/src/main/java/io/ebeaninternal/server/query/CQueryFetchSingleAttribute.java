package io.ebeaninternal.server.query;

import io.ebean.CancelableQuery;
import io.ebean.CountedValue;
import io.ebean.core.type.ScalarDataReader;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.type.RsetDataReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base compiled query request for single attribute queries.
 */
final class CQueryFetchSingleAttribute implements SpiProfileTransactionEvent, CancelableQuery {

  private final CQueryPlan queryPlan;
  private final OrmQueryRequest<?> request;
  private final BeanDescriptor<?> desc;
  private final SpiQuery<?> query;
  private final CQueryPredicates predicates;
  private final String sql;
  private RsetDataReader dataReader;
  private PreparedStatement pstmt;
  private String bindLog;
  private long executionTimeMicros;
  private int rowCount;
  private final ScalarDataReader<?> reader;
  private final boolean containsCounts;
  private long profileOffset;
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Create the Sql select based on the request.
   */
  CQueryFetchSingleAttribute(OrmQueryRequest<?> request, CQueryPredicates predicates, CQueryPlan queryPlan, boolean containsCounts) {
    this.request = request;
    this.queryPlan = queryPlan;
    this.query = request.query();
    this.sql = queryPlan.getSql();
    this.desc = request.descriptor();
    this.predicates = predicates;
    this.containsCounts = containsCounts;
    this.reader = queryPlan.getSingleAttributeScalarType();
    query.setGeneratedSql(sql);
  }

  /**
   * Return a summary description of this query.
   */
  String getSummary() {
    StringBuilder sb = new StringBuilder(80);
    sb.append("FindAttr exeMicros[").append(executionTimeMicros)
      .append("] rows[").append(rowCount)
      .append("] type[").append(desc.name())
      .append("] predicates[").append(predicates.getLogWhereSql())
      .append("] bind[").append(bindLog).append("]");
    return sb.toString();
  }

  long micros() {
    return executionTimeMicros;
  }

  /**
   * Execute the query returning the row count.
   */
  List<Object> findList() throws SQLException {
    long startNano = System.nanoTime();
    try {
      prepareExecute();
      List<Object> result = new ArrayList<>();
      while (dataReader.next()) {
        Object value = reader.read(dataReader);
        if (containsCounts) {
          value = new CountedValue<>(value, dataReader.getLong());
        }
        result.add(value);
        rowCount++;
      }
      executionTimeMicros = (System.nanoTime() - startNano) / 1000L;
      request.slowQueryCheck(executionTimeMicros, rowCount);
      if (queryPlan.executionTime(executionTimeMicros)) {
        queryPlan.captureBindForQueryPlan(predicates, executionTimeMicros);
      }
      getTransaction().profileEvent(this);
      return result;
    } finally {
      close();
    }
  }

  private SpiTransaction getTransaction() {
    return request.transaction();
  }

  /**
   * Return the bind log.
   */
  String getBindLog() {
    return bindLog;
  }

  /**
   * Return the generated sql.
   */
  String getGeneratedSql() {
    return sql;
  }

  private void prepareExecute() throws SQLException {
    lock.lock();
    try {
      query.checkCancelled();
      SpiTransaction t = getTransaction();
      profileOffset = t.profileOffset();
      Connection conn = t.getInternalConnection();
      pstmt = conn.prepareStatement(sql);
      if (query.getBufferFetchSizeHint() > 0) {
        pstmt.setFetchSize(query.getBufferFetchSizeHint());
      }
      if (query.getTimeout() > 0) {
        pstmt.setQueryTimeout(query.getTimeout());
      }
      bindLog = predicates.bind(pstmt, conn);
    } finally {
      lock.unlock();
    }
    dataReader = new RsetDataReader(request.dataTimeZone(), pstmt.executeQuery());
    query.checkCancelled();
  }

  /**
   * Close the resources.
   * <p>
   * The jdbc resultSet and statement need to be closed. Its important that
   * this method is called.
   * </p>
   */
  private void close() {
    try {
      if (dataReader != null) {
        dataReader.close();
        dataReader = null;
      }
    } catch (SQLException e) {
      CoreLog.log.error("Error closing DataReader", e);
    }
    JdbcClose.close(pstmt);
    pstmt = null;
  }

  @Override
  public void profile() {
    getTransaction()
      .profileStream()
      .addQueryEvent(query.profileEventId(), profileOffset, desc.name(), rowCount, query.getProfileId());
  }

  Set<String> getDependentTables() {
    return queryPlan.getDependentTables();
  }

  @Override
  public void cancel() {
    lock.lock();
    try {
      JdbcClose.cancel(pstmt);
    } finally {
      lock.unlock();
    }
  }
}
