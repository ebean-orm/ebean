package com.avaje.ebeaninternal.server.query;

import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.type.DataBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Executes the delete query.
 */
public class CQueryDelete {

  private static final Logger logger = LoggerFactory.getLogger(CQueryDelete.class);

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

  private int executionTimeMicros;

  private int rowCount;

  /**
   * Create the Sql select based on the request.
   */
  public CQueryDelete(OrmQueryRequest<?> request, CQueryPredicates predicates, String sql) {

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
    //noinspection StringBufferReplaceableByString
    StringBuilder sb = new StringBuilder(80);
    sb.append("Delete exeMicros[").append(executionTimeMicros)
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
   * Execute the query returning the row count.
   */
  public int delete() throws SQLException {

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
    try {
      if (pstmt != null) {
        pstmt.close();
        pstmt = null;
      }
    } catch (SQLException e) {
      logger.error(null, e);
    }
  }

}
