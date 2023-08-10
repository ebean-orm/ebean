package io.ebeaninternal.server.core;

import io.ebean.CancelableQuery;
import io.ebean.Transaction;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.persist.TrimLogSql;
import io.ebeaninternal.server.util.BindParamsParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps the objects involved in executing a SQL / Relational Query.
 */
public abstract class AbstractSqlQueryRequest implements CancelableQuery {

  protected final SpiSqlBinding query;
  protected final SpiEbeanServer server;
  protected SpiTransaction transaction;
  private boolean createdTransaction;
  protected String sql;
  protected ResultSet resultSet;
  protected String bindLog = "";
  protected PreparedStatement pstmt;
  protected long startNano;
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Create the BeanFindRequest.
   */
  AbstractSqlQueryRequest(SpiEbeanServer server, SpiSqlBinding query) {
    this.server = server;
    this.query = query;
    this.transaction = query.transaction();
    this.query.setCancelableQuery(this);
  }

  /**
   * Create a transaction if none currently exists.
   */
  public void initTransIfRequired() {
    if (transaction == null) {
      transaction = server.currentServerTransaction();
      if (transaction == null || !transaction.isActive()) {
        // create a local readOnly transaction
        transaction = server.createReadOnlyTransaction(null, query.isUseMaster());
        createdTransaction = true;
      }
    }
  }

  /**
   * End the transaction if it was locally created.
   */
  public void endTransIfRequired() {
    if (createdTransaction) {
      transaction.commit();
    }
  }

  protected void flushJdbcBatchOnQuery() {
    if (transaction.isFlushOnQuery()) {
      transaction.flush();
    }
  }

  public boolean isLogSql() {
    return transaction.isLogSql();
  }

  /**
   * Return the bindLog for this request.
   */
  public String getBindLog() {
    return bindLog;
  }

  /**
   * Set the resultSet and associated query plan if known.
   */
  abstract void setResultSet(ResultSet resultSet, Object queryPlanKey) throws SQLException;

  /**
   * Return true if we can navigate to the next row.
   */
  public abstract boolean next() throws SQLException;

  protected abstract void requestComplete();

  /**
   * Close the underlying resources.
   */
  public void close() {
    requestComplete();
    JdbcClose.close(resultSet);
    JdbcClose.close(pstmt);
  }

  /**
   * Prepare the SQL taking into account named bind parameters.
   */
  private void prepareSql() {
    String sql = query.getQuery();
    BindParams bindParams = query.getBindParams();
    if (!bindParams.isEmpty()) {
      // convert any named parameters if required
      sql = BindParamsParser.parse(bindParams, sql);
    }
    this.sql = limitOffset(sql);
  }

  private String limitOffset(String sql) {
    int firstRow = query.getFirstRow();
    int maxRows = query.getMaxRows();
    if (firstRow > 0 || maxRows > 0) {
      return server.databasePlatform().basicSqlLimiter().limit(sql, firstRow, maxRows);
    }
    return sql;
  }

  /**
   * Prepare and execute the SQL using the Binder.
   */
  public void executeSql(Binder binder) throws SQLException {
    startNano = System.nanoTime();
    executeAsSql(binder);
  }

  protected void executeAsSql(Binder binder) throws SQLException {
    lock.lock();
    try {
      query.checkCancelled();
      prepareSql();
      Connection conn = transaction.internalConnection();
      pstmt = conn.prepareStatement(sql);
      if (query.getTimeout() > 0) {
        pstmt.setQueryTimeout(query.getTimeout());
      }
      if (query.getBufferFetchSizeHint() > 0) {
        pstmt.setFetchSize(query.getBufferFetchSizeHint());
      }
      BindParams bindParams = query.getBindParams();
      if (!bindParams.isEmpty()) {
        this.bindLog = binder.bind(bindParams, pstmt, conn);
      }
      if (isLogSql()) {
        long micros = (System.nanoTime() - startNano) / 1000L;
        transaction.logSql("{0}; --bind({1}) --micros({2})", TrimLogSql.trim(sql), bindLog, micros);
      }
    } finally {
      lock.unlock();
    }
    setResultSet(pstmt.executeQuery(), null);
    query.checkCancelled();
  }

  /**
   * Return the SQL executed for this query.
   */
  public String getSql() {
    return sql;
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
