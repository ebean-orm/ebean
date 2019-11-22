package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.lib.Str;
import io.ebeaninternal.server.persist.BatchedPstmt;
import io.ebeaninternal.server.persist.BatchedPstmtHolder;
import io.ebeaninternal.server.persist.dmlbind.BindableRequest;
import io.ebeaninternal.server.type.DataBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Base class for Handler implementations.
 */
public abstract class DmlHandler implements PersistHandler, BindableRequest {

  private static final Logger logger = LoggerFactory.getLogger(DmlHandler.class);

  private static final int[] GENERATED_KEY_COLUMNS = new int[]{1};
  private static final int BATCHED_FIRST = 1;
  private static final int BATCHED = 2;

  /**
   * The originating request.
   */
  final PersistRequestBean<?> persistRequest;

  private final StringBuilder bindLog;

  final SpiTransaction transaction;

  private final boolean logLevelSql;

  private final long now;

  /**
   * The PreparedStatement used for the dml.
   */
  DataBind dataBind;

  BatchedPstmt batchedPstmt;

  String sql;

  private short batchedStatus;

  DmlHandler(PersistRequestBean<?> persistRequest) {
    this.now = System.currentTimeMillis();
    this.persistRequest = persistRequest;
    this.transaction = persistRequest.getTransaction();
    this.logLevelSql = transaction.isLogSql();
    if (logLevelSql) {
      this.bindLog = new StringBuilder(50);
    } else {
      this.bindLog = null;
    }
  }

  @Override
  public long now() {
    return now;
  }

  @Override
  public PersistRequestBean<?> getPersistRequest() {
    return persistRequest;
  }

  /**
   * Bind to the statement returning the DataBind.
   */
  DataBind bind(PreparedStatement stmt) {
    return new DataBind(persistRequest.getDataTimeZone(), stmt, transaction.getInternalConnection());
  }

  /**
   * Get the sql and bind the statement.
   */
  @Override
  public abstract void bind() throws SQLException;

  /**
   * Execute now for non-batch execution.
   */
  @Override
  public abstract int execute() throws SQLException;

  /**
   * Check the rowCount.
   */
  void checkRowCount(int rowCount) throws OptimisticLockException {
    try {
      persistRequest.checkRowCount(rowCount);
      persistRequest.postExecute();
    } catch (OptimisticLockException e) {
      // add the SQL and bind values to error message
      String m = e.getMessage() + " sql[" + sql + "] bind[" + bindLog + "]";
      persistRequest.getTransaction().logSummary("OptimisticLockException:" + m);
      throw new OptimisticLockException(m, null, e.getEntity());
    }
  }

  /**
   * Add this for batch execution.
   */
  @Override
  public void addBatch() throws SQLException {
    dataBind.getPstmt().addBatch();
  }

  /**
   * Close the underlying statement.
   */
  @Override
  public void close() {
    try {
      if (dataBind != null) {
        dataBind.close();
      }
    } catch (SQLException ex) {
      logger.error(null, ex);
    }
  }

  /**
   * Set the Id value that was bound. This value is used for logging summary
   * level information.
   */
  @Override
  public void setIdValue(Object idValue) {
    persistRequest.setBoundId(idValue);
  }

  /**
   * Log the sql to the transaction log.
   */
  void logSql(String sql) {
    if (logLevelSql) {
      switch (batchedStatus) {
        case BATCHED_FIRST: {
          transaction.logSql(sql);
          transaction.logSql(Str.add(" -- bind(", bindLog.toString(), ")"));
          return;
        }
        case BATCHED: {
          transaction.logSql(Str.add(" -- bind(", bindLog.toString(), ")"));
          return;
        }
        default: {
          transaction.logSql(Str.add(sql, "; -- bind(", bindLog.toString(), ")"));
        }
      }
    }
  }

  /**
   * Bind a raw value. Used to bind the discriminator column.
   */
  @Override
  public void bind(Object value, int sqlType) throws SQLException {
    if (logLevelSql) {
      if (bindLog.length() > 0) {
        bindLog.append(",");
      }
      if (value == null) {
        bindLog.append("null");
      } else {
        String sval = value.toString();
        if (sval.length() > 50) {
          bindLog.append(sval, 0, 47).append("...");
        } else {
          bindLog.append(sval);
        }
      }
    }
    dataBind.setObject(value, sqlType);
  }

  @Override
  public void bindNoLog(Object value, int sqlType, String logPlaceHolder) throws SQLException {
    if (logLevelSql) {
      if (bindLog.length() > 0) {
        bindLog.append(",");
      }
      bindLog.append(logPlaceHolder);
    }
    dataBind.setObject(value, sqlType);
  }

  /**
   * Bind the value to the preparedStatement.
   */
  @Override
  public void bind(Object value, BeanProperty prop) throws SQLException {
    bindInternal(logLevelSql, value, prop);
  }

  /**
   * Bind the value to the preparedStatement without logging.
   */
  @Override
  public void bindNoLog(Object value, BeanProperty prop) throws SQLException {
    bindInternal(false, value, prop);
  }

  private void bindInternal(boolean log, Object value, BeanProperty prop) throws SQLException {

    if (log) {
      if (bindLog.length() > 0) {
        bindLog.append(",");
      }
      if (prop.isLob()) {
        bindLog.append("[LOB]");
      } else {
        String sv = String.valueOf(value);
        if (sv.length() > 50) {
          sv = sv.substring(0, 47) + "...";
        }
        bindLog.append(sv);
      }
    }
    // do the actual binding to PreparedStatement
    prop.bind(dataBind, value);
  }

  /**
   * Check with useGeneratedKeys to get appropriate PreparedStatement.
   */
  PreparedStatement getPstmt(SpiTransaction t, String sql, boolean genKeys) throws SQLException {

    Connection conn = t.getInternalConnection();
    if (genKeys) {
      // the Id generated is always the first column
      // Required to stop Oracle10 giving us Oracle rowId??
      // Other jdbc drivers seem fine without this hint.
      return conn.prepareStatement(sql, GENERATED_KEY_COLUMNS);

    } else {
      return conn.prepareStatement(sql);
    }
  }

  /**
   * Return a prepared statement taking into account batch requirements.
   */
  PreparedStatement getPstmt(SpiTransaction t, String sql, PersistRequestBean<?> request, boolean genKeys) throws SQLException {

    BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
    batchedPstmt = batch.getBatchedPstmt(sql, request);
    if (batchedPstmt != null) {
      batchedStatus = BATCHED;
      return batchedPstmt.getStatement();
    }

    batchedStatus = BATCHED_FIRST;
    PreparedStatement stmt = getPstmt(t, sql, genKeys);

    batchedPstmt = new BatchedPstmt(stmt, genKeys, sql, t);
    batch.addStmt(batchedPstmt, request);
    return stmt;
  }

}
