package io.ebeaninternal.server.query;

import io.ebean.CancelableQuery;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executes the select row count query.
 */
final class CQueryRowCount implements SpiProfileTransactionEvent, CancelableQuery {

  private final CQueryPlan queryPlan;
  private final OrmQueryRequest<?> request;
  private final BeanDescriptor<?> desc;
  private final SpiQuery<?> query;
  private final CQueryPredicates predicates;
  private final String sql;
  private ResultSet rset;
  private PreparedStatement pstmt;
  private String bindLog;
  private long executionTimeMicros;
  private int rowCount;
  private long profileOffset;
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Create the Sql select based on the request.
   */
  CQueryRowCount(CQueryPlan queryPlan, OrmQueryRequest<?> request, CQueryPredicates predicates) {
    this.queryPlan = queryPlan;
    this.request = request;
    this.query = request.query();
    this.sql = queryPlan.sql();
    this.desc = request.descriptor();
    this.predicates = predicates;
    query.setGeneratedSql(sql);
  }

  /**
   * Return a summary description of this query.
   */
  public String summary() {
    //noinspection StringBufferReplaceableByString
    StringBuilder sb = new StringBuilder(80);
    sb.append("FindCount exeMicros[").append(executionTimeMicros)
      .append("] rows[").append(rowCount)
      .append("] type[").append(desc.fullName())
      .append("] predicates[").append(predicates.logWhereSql())
      .append("] bind[").append(bindLog).append(']');

    return sb.toString();
  }

  /**
   * Return the bind log.
   */
  public String bindLog() {
    return bindLog;
  }

  /**
   * Return the generated sql.
   */
  public String generatedSql() {
    return sql;
  }

  long micros() {
    return executionTimeMicros;
  }

  /**
   * Execute the query returning the row count.
   */
  public int findCount() throws SQLException {
    long startNano = System.nanoTime();
    try {
      SpiTransaction t = transaction();
      profileOffset = t.profileOffset();
      Connection conn = t.internalConnection();
      lock.lock();
      try {
        query.checkCancelled();
        pstmt = conn.prepareStatement(sql);
        if (query.timeout() > 0) {
          pstmt.setQueryTimeout(query.timeout());
        }
        bindLog = predicates.bind(pstmt, conn);
      } finally {
        lock.unlock();
      }
      rset = pstmt.executeQuery();
      query.checkCancelled();
      if (!rset.next()) {
        throw new PersistenceException("Expecting 1 row but got none?");
      }
      rowCount = rset.getInt(1);
      executionTimeMicros = (System.nanoTime() - startNano) / 1000L;
      request.slowQueryCheck(executionTimeMicros, rowCount);
      if (queryPlan.executionTime(executionTimeMicros)) {
        queryPlan.captureBindForQueryPlan(predicates, executionTimeMicros);
      }
      t.profileEvent(this);
      return rowCount;
    } finally {
      close();
    }
  }

  private SpiTransaction transaction() {
    return request.transaction();
  }

  /**
   * Close the resources.
   */
  private void close() {
    JdbcClose.close(rset);
    JdbcClose.close(pstmt);
    rset = null;
    pstmt = null;
  }

  @Override
  public void profile() {
    transaction()
      .profileStream()
      .addQueryEvent(query.profileEventId(), profileOffset, desc.name(), rowCount, query.profileId());
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
