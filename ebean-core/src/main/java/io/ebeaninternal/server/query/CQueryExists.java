package io.ebeaninternal.server.query;

import io.ebean.CancelableQuery;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executes a select exists(...) query returning a boolean result.
 */
final class CQueryExists implements SpiProfileTransactionEvent, CancelableQuery {

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
  private boolean result;
  private long profileOffset;
  private final ReentrantLock lock = new ReentrantLock();

  CQueryExists(CQueryPlan queryPlan, OrmQueryRequest<?> request, CQueryPredicates predicates) {
    this.queryPlan = queryPlan;
    this.request = request;
    this.query = request.query();
    this.sql = queryPlan.sql();
    this.desc = request.descriptor();
    this.predicates = predicates;
    query.setGeneratedSql(sql);
  }

  public String summary() {
    return "FindExists exeMicros[" + executionTimeMicros
      + "] result[" + result
      + "] type[" + desc.fullName()
      + "] predicates[" + predicates.logWhereSql()
      + "] bind[" + bindLog + ']';
  }

  public String bindLog() {
    return bindLog;
  }

  public String generatedSql() {
    return sql;
  }

  long micros() {
    return executionTimeMicros;
  }

  /**
   * Execute the query returning the exists boolean result.
   */
  public boolean findExists() throws SQLException {
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
        throw new jakarta.persistence.PersistenceException("Expecting 1 row from exists query but got none?");
      }
      result = rset.getBoolean(1);
      executionTimeMicros = (System.nanoTime() - startNano) / 1000L;
      request.slowQueryCheck(executionTimeMicros, result ? 1 : 0);
      if (queryPlan.executionTime(executionTimeMicros)) {
        queryPlan.captureBindForQueryPlan(predicates, executionTimeMicros);
      }
      t.profileEvent(this);
      return result;
    } finally {
      close();
    }
  }

  private SpiTransaction transaction() {
    return request.transaction();
  }

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
      .addQueryEvent(query.profileEventId(), profileOffset, desc.name(), result ? 1 : 0, query.profileId(), queryPlan.hash(), query.getGeneratedSql());
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
