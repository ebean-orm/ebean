package com.avaje.ebeaninternal.server.query;

import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Executes the delete query.
 */
class CQueryUpdate {

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

  private int executionTimeMicros;

  private int rowCount;

  /**
   * Create the Sql select based on the request.
   */
  CQueryUpdate(String type, OrmQueryRequest<?> request, CQueryPredicates predicates, String sql) {
    this.type = type;
    this.request = request;
    this.query = request.getQuery();
    this.sql = sql;
    query.setGeneratedSql(sql);
    this.desc = request.getBeanDescriptor();
    this.predicates = predicates;
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

      SpiTransaction t = request.getTransaction();
      Connection conn = t.getInternalConnection();
      pstmt = conn.prepareStatement(sql);

      if (query.getTimeout() > 0) {
        pstmt.setQueryTimeout(query.getTimeout());
      }

      bindLog = predicates.bind(pstmt, conn);
      rowCount = pstmt.executeUpdate();

      long exeNano = System.nanoTime() - startNano;
      executionTimeMicros = (int) exeNano / 1000;

      return rowCount;

    } finally {
      close();
    }
  }

  /**
   * Close the resources.
   */
  private void close() {
    UtilJdbc.close(pstmt);
    pstmt = null;
  }

}
