package io.ebeaninternal.server.query;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Executes the delete query.
 */
class CQueryUpdate implements SpiProfileTransactionEvent {

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

  private final String type;

  /**
   * The statement used to create the resultSet.
   */
  private PreparedStatement pstmt;

  private String bindLog;

  private long executionTimeMicros;

  private int rowCount;

  private long profileOffset;

  /**
   * Create the Sql select based on the request.
   */
  CQueryUpdate(String type, OrmQueryRequest<?> request, CQueryPredicates predicates, CQueryPlan queryPlan) {
    this.type = type;
    this.request = request;
    this.queryPlan = queryPlan;
    this.query = request.getQuery();
    this.sql = queryPlan.getSql();
    this.desc = request.getBeanDescriptor();
    this.predicates = predicates;
    query.setGeneratedSql(sql);
  }

  /**
   * Return a summary description of this query.
   */
  public String getSummary() {
    StringBuilder sb = new StringBuilder(80);
    sb.append(type).append(" exeMicros[").append(executionTimeMicros)
      .append("] rows[").append(rowCount)
      .append("] type[").append(desc.getName())
      .append("] predicates[").append(predicates.getLogWhereSql())
      .append("] bind[").append(bindLog).append("]");
    return sb.toString();
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
      pstmt = conn.prepareStatement(sql);

      if (query.getTimeout() > 0) {
        pstmt.setQueryTimeout(query.getTimeout());
      }

      bindLog = predicates.bind(pstmt, conn);
      rowCount = pstmt.executeUpdate();

      executionTimeMicros = (System.nanoTime() - startNano) / 1000L;
      request.slowQueryCheck(executionTimeMicros, rowCount);
      queryPlan.executionTime(rowCount, executionTimeMicros, null);
      getTransaction().profileEvent(this);
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
      .addQueryEvent(query.profileEventId(), profileOffset, desc.getProfileId(), rowCount, query.getProfileId());
  }
}
