package io.ebeaninternal.server.query;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Executes the select row count query.
 */
class CQueryRowCount implements SpiProfileTransactionEvent {

  private final CQueryPlan queryPlan;

  /**
   * The overall find request wrapper object.
   */
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
   * The resultSet that is read and converted to objects.
   */
  private ResultSet rset;

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
  CQueryRowCount(CQueryPlan queryPlan, OrmQueryRequest<?> request, CQueryPredicates predicates) {
    this.queryPlan = queryPlan;
    this.request = request;
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
    //noinspection StringBufferReplaceableByString
    StringBuilder sb = new StringBuilder(80);
    sb.append("FindCount exeMicros[").append(executionTimeMicros)
      .append("] rows[").append(rowCount)
      .append("] type[").append(desc.getFullName())
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
   * Execute the query returning the row count.
   */
  public int findCount() throws SQLException {

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
      rset = pstmt.executeQuery();
      if (!rset.next()) {
        throw new PersistenceException("Expecting 1 row but got none?");
      }

      rowCount = rset.getInt(1);

      executionTimeMicros = (System.nanoTime() - startNano) / 1000L;
      queryPlan.executionTime(rowCount, executionTimeMicros, query.getParentNode());
      request.slowQueryCheck(executionTimeMicros, rowCount);
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
    JdbcClose.close(rset);
    JdbcClose.close(pstmt);
    rset = null;
    pstmt = null;
  }

  @Override
  public void profile() {
    getTransaction()
      .profileStream()
      .addQueryEvent(query.profileEventId(), profileOffset, desc.getProfileId(), rowCount, query.getProfileId());
  }

  Set<String> getDependentTables() {
    return queryPlan.getDependentTables();
  }
}
