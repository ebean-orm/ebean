package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiTransaction;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Factory for creating Statements.
 * <p>
 * This is only used by CallableSql and UpdateSql requests and does not support
 * getGeneratedKeys.
 * </p>
 */
public class PstmtFactory {

  public PstmtFactory() {
  }

  /**
   * Get a callable statement without any batching.
   */
  public CallableStatement getCstmt(SpiTransaction t, String sql) throws SQLException {
    Connection conn = t.getInternalConnection();
    return conn.prepareCall(sql);
  }

  /**
   * Get a prepared statement without any batching.
   */
  public PreparedStatement getPstmt(SpiTransaction t, String sql) throws SQLException {
    Connection conn = t.getInternalConnection();
    return conn.prepareStatement(sql);
  }

  /**
   * Return a prepared statement taking into account batch requirements.
   */
  public PreparedStatement getPstmt(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe) throws SQLException {

    BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
    PreparedStatement stmt = batch.getStmt(sql, batchExe);

    if (stmt != null) {
      return stmt;
    }

    if (logSql) {
      t.logSql(TrimLogSql.trim(sql));
    }

    Connection conn = t.getInternalConnection();
    stmt = conn.prepareStatement(sql);

    BatchedPstmt bs = new BatchedPstmt(stmt, false, sql, t);
    batch.addStmt(bs, batchExe);
    return stmt;
  }

  /**
   * Return a callable statement taking into account batch requirements.
   */
  public CallableStatement getCstmt(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe) throws SQLException {

    BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
    CallableStatement stmt = (CallableStatement) batch.getStmt(sql, batchExe);

    if (stmt != null) {
      return stmt;
    }

    if (logSql) {
      t.logSql(sql);
    }

    Connection conn = t.getInternalConnection();
    stmt = conn.prepareCall(sql);

    BatchedPstmt bs = new BatchedPstmt(stmt, false, sql, t);
    batch.addStmt(bs, batchExe);
    return stmt;
  }
}
