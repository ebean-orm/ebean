package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.lib.Str;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.persist.PersistExecute;
import io.ebeaninternal.server.persist.TrimLogSql;

/**
 * Persist request specifically for CallableSql.
 */
public final class PersistRequestUpdateSql extends PersistRequest {

  public enum SqlType {
    SQL_UPDATE, SQL_DELETE, SQL_INSERT, SQL_UNKNOWN
  }

  private final SpiSqlUpdate updateSql;

  private int rowCount;

  private String bindLog;

  private SqlType sqlType;

  private String tableName;

  private boolean addBatch;

  private final boolean forceNoBatch;

  private boolean batchThisRequest;

  public PersistRequestUpdateSql(SpiEbeanServer server, SpiSqlUpdate sqlUpdate,
                                 SpiTransaction t, PersistExecute persistExecute, boolean forceNoBatch) {

    super(server, t, persistExecute, sqlUpdate.getLabel());
    this.type = Type.UPDATESQL;
    this.updateSql = sqlUpdate;
    this.forceNoBatch = forceNoBatch;
    updateSql.reset();
  }

  public PersistRequestUpdateSql(SpiEbeanServer server, SpiSqlUpdate sqlUpdate,
                                 SpiTransaction t, PersistExecute persistExecute) {
    this(server, sqlUpdate, t, persistExecute, false);
  }

  @Override
  public void profile(long offset, int flushCount) {
    profileBase(EVT_UPDATESQL, offset, (short)0, flushCount);
  }

  /**
   * Add this statement to JDBC batch for later execution.
   */
  public int addBatch() {
    this.addBatch = true;
    return executeOrQueue();
  }

  /**
   * Execute using jdbc batch.
   */
  public void executeAddBatch() {
    this.addBatch = true;
    persistExecute.executeSqlUpdate(this);
  }

  /**
   * Add this request to BatchControl to flush later.
   */
  public void addToFlushQueue(boolean early) {
    BatchControl control = transaction.getBatchControl();
    if (control == null) {
      control = persistExecute.createBatchControl(transaction);
    }
    control.addToFlushQueue(this, early);
  }

  @Override
  public int executeNow() {
    return persistExecute.executeSqlUpdate(this);
  }

  @Override
  public boolean isBatchThisRequest() {
    return !forceNoBatch && (addBatch || super.isBatchThisRequest());
  }

  @Override
  public int executeOrQueue() {
    return executeStatement();
  }

  /**
   * Return the UpdateSql.
   */
  public SpiSqlUpdate getUpdateSql() {
    return updateSql;
  }

  /**
   * No concurrency checking so just note the rowCount.
   */
  @Override
  public void checkRowCount(int count) {
    this.rowCount = count;
  }

  /**
   * Not called for this type of request.
   */
  @Override
  public void setGeneratedKey(Object idValue) {
    updateSql.setGeneratedKey(idValue);
  }

  public boolean isGetGeneratedKeys() {
    return updateSql.isGetGeneratedKeys();
  }

  /**
   * Specify the type of statement executed. Used to automatically register
   * with the transaction event.
   */
  public void setType(SqlType sqlType, String tableName) {
    this.sqlType = sqlType;
    this.tableName = tableName;
  }

  /**
   * Set the bound values.
   */
  public void setBindLog(String bindLog) {
    this.bindLog = bindLog;
  }

  public void startBind(boolean batchThisRequest) {
    this.batchThisRequest = batchThisRequest;
    super.startBind(batchThisRequest);
  }

  /**
   * Log the sql bind used with jdbc batch.
   */
  public void logSqlBatchBind() {
    if (transaction.isLogSql()) {
      transaction.logSql(Str.add(" -- bind(", bindLog, ")"));
    }
  }

  /**
   * Perform post execute processing.
   */
  @Override
  public void postExecute() {
    if (startNanos > 0) {
      persistExecute.collectSqlUpdate(label, startNanos, rowCount);
    }
    if (transaction.isLogSql() && !batchThisRequest) {
      transaction.logSql(Str.add(TrimLogSql.trim(updateSql.getGeneratedSql()), "; -- bind(", bindLog, ") rows(", String.valueOf(rowCount), ")"));
    }

    if (updateSql.isAutoTableMod()) {
      // add the modification info to the TransactionEvent
      // this is used to invalidate cached objects etc
      switch (sqlType) {
        case SQL_INSERT:
          transaction.getEvent().add(tableName, true, false, false);
          break;
        case SQL_UPDATE:
          transaction.getEvent().add(tableName, false, true, false);
          break;
        case SQL_DELETE:
          transaction.getEvent().add(tableName, false, false, true);
          break;
        case SQL_UNKNOWN:
          transaction.markNotQueryOnly();

        default:
          break;
      }
    }
  }

}
