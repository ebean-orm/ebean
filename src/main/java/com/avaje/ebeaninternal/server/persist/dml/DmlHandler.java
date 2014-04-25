package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.BatchedPstmt;
import com.avaje.ebeaninternal.server.persist.BatchedPstmtHolder;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;
import com.avaje.ebeaninternal.server.type.DataBind;

/**
 * Base class for Handler implementations.
 */
public abstract class DmlHandler implements PersistHandler, BindableRequest {

  private static final Logger logger = LoggerFactory.getLogger(DmlHandler.class);

  /**
   * The originating request.
   */
  protected final PersistRequestBean<?> persistRequest;

  protected final StringBuilder bindLog;

  protected final SpiTransaction transaction;

  protected final boolean emptyStringToNull;

  protected final boolean logLevelSql;

  /**
   * The PreparedStatement used for the dml.
   */
  protected DataBind dataBind;

  protected String sql;

  protected ArrayList<UpdateGenValue> updateGenValues;

  protected DmlHandler(PersistRequestBean<?> persistRequest, boolean emptyStringToNull) {
    this.persistRequest = persistRequest;
    this.emptyStringToNull = emptyStringToNull;
    this.transaction = persistRequest.getTransaction();
    this.logLevelSql = transaction.isLogSql();
    if (logLevelSql) {
      this.bindLog = new StringBuilder();
    } else {
      this.bindLog = null;
    }
  }

  public PersistRequestBean<?> getPersistRequest() {
    return persistRequest;
  }

  /**
   * Get the sql and bind the statement.
   */
  public abstract void bind() throws SQLException;

  /**
   * Execute now for non-batch execution.
   */
  public abstract void execute() throws SQLException;

  /**
   * Check the rowCount.
   */
  protected void checkRowCount(int rowCount) throws SQLException, OptimisticLockException {
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
  public void addBatch() throws SQLException {
    PstmtBatch pstmtBatch = persistRequest.getPstmtBatch();
    if (pstmtBatch != null) {
      pstmtBatch.addBatch(dataBind.getPstmt());
    } else {
      dataBind.getPstmt().addBatch();
    }
  }

  /**
   * Close the underlying statement.
   */
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
   * Return the bind log.
   */
  public String getBindLog() {
    return bindLog == null ? "" : bindLog.toString();
  }

  /**
   * Set the Id value that was bound. This value is used for logging summary
   * level information.
   */
  public void setIdValue(Object idValue) {
    persistRequest.setBoundId(idValue);
  }

  /**
   * Log the sql to the transaction log.
   */
  protected void logSql(String sql) {
    if (logLevelSql) {
      if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
        sql += "; --bind(" + bindLog + ")";
      }
      transaction.logSql(sql);
    }
  }

  /**
   * Bind a raw value. Used to bind the discriminator column.
   */
  public Object bind(String propName, Object value, int sqlType) throws SQLException {
    if (logLevelSql) {
      if (value == null) {
        bindLog.append("null");
      } else {
        String sval = value.toString();
        if (sval.length() > 50) {
          bindLog.append(sval.substring(0, 47)).append("...");
        } else {
          bindLog.append(sval);
        }
      }
      bindLog.append(",");
    }
    dataBind.setObject(value, sqlType);
    return value;
  }

  public Object bindNoLog(Object value, int sqlType, String logPlaceHolder) throws SQLException {
    if (logLevelSql) {
      bindLog.append(logPlaceHolder).append(" ");
    }
    dataBind.setObject(value, sqlType);
    return value;
  }

  /**
   * Bind the value to the preparedStatement.
   */
  public Object bind(Object value, BeanProperty prop, String propName) throws SQLException {
    return bindInternal(logLevelSql, value, prop, propName);
  }

  /**
   * Bind the value to the preparedStatement without logging.
   */
  public Object bindNoLog(Object value, BeanProperty prop, String propName) throws SQLException {
    return bindInternal(false, value, prop, propName);
  }

  private Object bindInternal(boolean log, Object value, BeanProperty prop, String propName) throws SQLException {

    if (log) {
      if (prop.isLob()) {
        bindLog.append("[LOB]");
      } else {
        String sv = String.valueOf(value);
        if (sv.length() > 50) {
          sv = sv.substring(0, 47) + "...";
        }
        bindLog.append(sv);
      }
      bindLog.append(",");
    }
    // do the actual binding to PreparedStatement
    prop.bind(dataBind, value);
    return value;
  }

  /**
   * Register a generated value on a update. This can not be set to the bean
   * until after the where clause has been bound for concurrency checking.
   * <p>
   * GeneratedProperty values are likely going to be used for optimistic
   * concurrency checking. This includes 'counter' and 'update timestamp'
   * generation.
   * </p>
   */
  public void registerUpdateGenValue(BeanProperty prop, EntityBean bean, Object value) {
    if (updateGenValues == null) {
      updateGenValues = new ArrayList<UpdateGenValue>();
    }
    updateGenValues.add(new UpdateGenValue(prop, bean, value));
  }

  /**
   * Set any update generated values to the bean. Must be called after where
   * clause has been bound.
   */
  public void setUpdateGenValues() {
    if (updateGenValues != null) {
      for (int i = 0; i < updateGenValues.size(); i++) {
        UpdateGenValue updGenVal = updateGenValues.get(i);
        updGenVal.setValue();
      }
    }
  }

  /**
   * Check with useGeneratedKeys to get appropriate PreparedStatement.
   */
  protected PreparedStatement getPstmt(SpiTransaction t, String sql, boolean genKeys)
      throws SQLException {
    
    Connection conn = t.getInternalConnection();
    if (genKeys) {
      // the Id generated is always the first column
      // Required to stop Oracle10 giving us Oracle rowId??
      // Other jdbc drivers seem fine without this hint.
      int[] columns = { 1 };
      return conn.prepareStatement(sql, columns);

    } else {
      return conn.prepareStatement(sql);
    }
  }

  /**
   * Return a prepared statement taking into account batch requirements.
   */
  protected PreparedStatement getPstmt(SpiTransaction t, String sql, PersistRequestBean<?> request,
      boolean genKeys) throws SQLException {

    BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
    PreparedStatement stmt = batch.getStmt(sql, request);

    if (stmt != null) {
      return stmt;
    }

    if (logLevelSql) {
      t.logSql(sql);
    }

    stmt = getPstmt(t, sql, genKeys);

    PstmtBatch pstmtBatch = request.getPstmtBatch();
    if (pstmtBatch != null) {
      pstmtBatch.setBatchSize(stmt, t.getBatchControl().getBatchSize());
    }

    BatchedPstmt bs = new BatchedPstmt(stmt, genKeys, sql, request.getPstmtBatch(), true);
    batch.addStmt(bs, request);
    return stmt;
  }

  /**
   * Hold the values from GeneratedValue that need to be set to the bean
   * property after the where clause has been built.
   */
  private static final class UpdateGenValue {

    private final BeanProperty property;

    private final EntityBean bean;

    private final Object value;

    private UpdateGenValue(BeanProperty property, EntityBean bean, Object value) {
      this.property = property;
      this.bean = bean;
      this.value = value;
    }

    /**
     * Set the value to the bean property.
     */
    private void setValue() {
      // support PropertyChangeSupport
      property.setValueIntercept(bean, value);
    }
  }
}
