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
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executes the update query.
 */
class CQueryUpdate implements SpiProfileTransactionEvent, CancelableQuery {

  private final CQueryPlan queryPlan;

  private final OrmQueryRequest<?> request;

  private final BeanDescriptor<?> desc;

  private final SpiQuery<?> query;

  /**
   * Where clause predicates.
   */
  private final CQueryPredicates predicates;

  /**
   * The final sql that is generated.
   */
  private final String sql;

  /**
   * The statement used to create the resultSet.
   */
  private PreparedStatement pstmt;

  private String bindLog;

  private int rowCount;

  private long profileOffset;

  private final ReentrantLock lock = new ReentrantLock();
  
  /**
   * Create the Sql select based on the request.
   */
  CQueryUpdate(OrmQueryRequest<?> request, CQueryPredicates predicates, CQueryPlan queryPlan) {
    this.request = request;
    this.queryPlan = queryPlan;
    this.query = request.getQuery();
    this.sql = queryPlan.getSql();
    this.desc = request.getBeanDescriptor();
    this.predicates = predicates;
    query.setGeneratedSql(sql);
  }

  /**
   * Return the bind log.
   */
  public String getBindLog() {
    return bindLog;
  }

  /**
   * Return the generated sql.
   */
  public String getGeneratedSql() {
    return sql;
  }

  /**
   * Execute the update or delete statement returning the row count.
   */
  public int execute() throws SQLException {

    long startNano = System.nanoTime();
    try {
      SpiTransaction t = getTransaction();
      profileOffset = t.profileOffset();
      Connection conn = t.getInternalConnection();
      lock.lock();
      try {
        query.checkCancelled();
        pstmt = conn.prepareStatement(sql);

        if (query.getTimeout() > 0) {
          pstmt.setQueryTimeout(query.getTimeout());
        }

        bindLog = predicates.bind(pstmt, conn);
      } finally {
        lock.unlock();
      }
      rowCount = pstmt.executeUpdate();
      query.checkCancelled();
      
      long executionTimeMicros = (System.nanoTime() - startNano) / 1000L;
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

  private SpiTransaction getTransaction() {
    return request.getTransaction();
  }

  /**
   * Close the resources.
   */
  private void close() {
    JdbcClose.close(pstmt);
    pstmt = null;
  }

  @Override
  public void profile() {
    getTransaction()
      .profileStream()
      .addQueryEvent(query.profileEventId(), profileOffset, desc.getName(), rowCount, query.getProfileId());
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
