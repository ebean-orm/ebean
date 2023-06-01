package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiTransaction;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Factory for creating Statements.
 * <p>
 * This is only used by CallableSql and UpdateSql requests and does not support
 * getGeneratedKeys.
 * </p>
 */
final class PstmtFactory {

  /**
   * Get a callable statement without any batching.
   */
  CallableStatement cstmt(SpiTransaction t, String sql) throws SQLException {
    Connection conn = t.internalConnection();
    return conn.prepareCall(sql);
  }

  /**
   * Get a prepared statement without any batching.
   */
  PreparedStatement pstmt(SpiTransaction t, String sql, boolean getGeneratedKeys) throws SQLException {
    Connection conn = t.internalConnection();
    if (getGeneratedKeys) {
      return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    } else {
      return conn.prepareStatement(sql);
    }
  }

  /**
   * Return a prepared statement taking into account batch requirements.
   */
  PreparedStatement pstmtBatch(SpiTransaction t, String sql, BatchPostExecute batchExe) throws SQLException {
    BatchedPstmtHolder batch = t.batchControl().pstmtHolder();
    BatchedPstmt existingStmt = batch.batchedPstmt(sql);
    if (existingStmt != null) {
      if (existingStmt.isEmpty() && t.isLogSql()) {
        t.logSql(TrimLogSql.trim(sql));
      }
      return existingStmt.statement(batchExe);
    }
    if (t.isLogSql()) {
      t.logSql(TrimLogSql.trim(sql));
    }
    Connection conn = t.internalConnection();
    PreparedStatement stmt = conn.prepareStatement(sql);
    BatchedPstmt bs = new BatchedPstmt(stmt, false, sql, t);
    batch.addStmt(bs, batchExe);
    return stmt;
  }

  /**
   * Return a callable statement taking into account batch requirements.
   */
  CallableStatement cstmtBatch(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe) throws SQLException {
    BatchedPstmtHolder batch = t.batchControl().pstmtHolder();
    CallableStatement stmt = (CallableStatement) batch.stmt(sql, batchExe);
    if (stmt != null) {
      return stmt;
    }
    if (logSql) {
      t.logSql(sql);
    }
    Connection conn = t.internalConnection();
    stmt = conn.prepareCall(sql);
    BatchedPstmt bs = new BatchedPstmt(stmt, false, sql, t);
    batch.addStmt(bs, batchExe);
    return stmt;
  }
}
