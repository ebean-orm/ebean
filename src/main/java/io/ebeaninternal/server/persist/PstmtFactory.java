package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.type.DataBind;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Factory for creating DataBinds based on statements.
 * <p>
 * This is only used by CallableSql and UpdateSql requests and does not support
 * getGeneratedKeys.
 * </p>
 */
class PstmtFactory {

  private final DataTimeZone dataTimeZone;

  PstmtFactory(DataTimeZone dataTimeZone) {
    this.dataTimeZone = dataTimeZone;
  }

  /**
   * Get a prepared statement without any batching. NOTE: you must close the containing statement.
   */
  public PreparedStatement getPstmt(SpiTransaction t, boolean logSql, String sql, boolean getGeneratedKeys) throws SQLException {
    if (logSql) {
      t.logSql(TrimLogSql.trim(sql));
    }
    Connection conn = t.getInternalConnection();
    PreparedStatement stmt;
    if (getGeneratedKeys) {
      stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    } else {
      stmt = conn.prepareStatement(sql);
    }
    return stmt;
  }

  /**
   * Returns a non batched databind, that will close the wrapped statement.
   */
  public DataBind getPDataBind(SpiTransaction t, boolean logSql, String sql, boolean getGeneratedKeys) throws SQLException {
    final PreparedStatement stmt = getPstmt(t, logSql, sql, getGeneratedKeys);
    return new DataBind(dataTimeZone, stmt, t.getInternalConnection());
  }

  /**
   * Return a DataBind based on a prepared statement taking into account batch requirements.
   */
  public DataBind getBatchedPDataBind(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe) throws SQLException {

    BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
    BatchedPstmt bs = batch.getStmt(sql, batchExe);

    Connection conn = t.getInternalConnection();
    if (bs == null) {
      if (logSql) {
        t.logSql(TrimLogSql.trim(sql));
      }
      PreparedStatement stmt = conn.prepareStatement(sql);
      bs = new BatchedPstmt(stmt, false, sql, t);
      batch.addStmt(bs, batchExe);
    }

    return new DataBind(dataTimeZone, bs, conn);
  }


  /**
   * Get a callable statement without any batching.
   */
  public CallableStatement getCStatement(SpiTransaction t, boolean logSql, String sql) throws SQLException {
    Connection conn = t.getInternalConnection();
    if (logSql) {
      t.logSql(TrimLogSql.trim(sql));
    }
    CallableStatement stmt = conn.prepareCall(sql);
    return stmt;
  }

  /**
   * Returns a non batched databind. NOTE: you must close the containing statement.
   */
  public DataBind getCDataBind(SpiTransaction t, boolean logSql, String sql) throws SQLException {
    final PreparedStatement stmt = getCStatement(t, logSql, sql);
    return new DataBind(dataTimeZone, stmt, t.getInternalConnection());
  }
  /**
   * Return a DataBind based on callable statement taking into account batch requirements.
   */
  public DataBind getBatchedCDataBind(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe) throws SQLException {

    BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
    BatchedPstmt bs = batch.getStmt(sql, batchExe);
    Connection conn = t.getInternalConnection();

    if (bs == null) {
      if (logSql) {
        t.logSql(TrimLogSql.trim(sql));
      }

      CallableStatement stmt = conn.prepareCall(sql);

      bs = new BatchedPstmt(stmt, false, sql, t);
      batch.addStmt(bs, batchExe);
    }
    return new DataBind(dataTimeZone, bs, conn);
  }
}
