package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.persist.PersistExecute;
import io.ebeaninternal.server.persist.TrimLogSql;

import java.util.List;

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
  private boolean flushQueue;

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
    profileBase(EVT_UPDATESQL, offset, "", flushCount);
  }

  /**
   * Add this statement to JDBC batch for later execution.
   */
  public void addBatch() {
    this.addBatch = true;
    executeStatement(true);
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
  public void addToFlushQueue(int pos) {
    BatchControl control = transaction.batchControl();
    if (control == null) {
      control = persistExecute.createBatchControl(transaction);
    }
    flushQueue = true;
    control.addToFlushQueue(this, pos);
  }

  @Override
  public boolean isFlushQueue() {
    return flushQueue;
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
  public SpiSqlUpdate updateSql() {
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

  @Override
  public void startBind(boolean batchThisRequest) {
    this.batchThisRequest = batchThisRequest;
    super.startBind(batchThisRequest);
  }

  /**
   * Log the sql bind used with jdbc batch.
   */
  public void logSqlBatchBind() {
    if (transaction.isLogSql()) {
      transaction.logSql(" -- bind({0})", bindLog);
    }
  }

  /**
   * Perform post execute processing.
   */
  @Override
  public void postExecute() {
    if (sqlType != SqlType.SQL_INSERT && !transaction.isAutoPersistUpdates()) {
      List<BeanDescriptor<?>> descriptors = server.descriptors(tableName);
      if (descriptors != null) {
        for (BeanDescriptor<?> descriptor : descriptors) {
          descriptor.contextClear(transaction.persistenceContext());
        }
      }
    }
    if (startNanos > 0) {
      persistExecute.collectSqlUpdate(label, startNanos);
    }
    if (transaction.isLogSql() && !batchThisRequest) {
      transaction.logSql("{0}; -- bind({1}) rows({2})", TrimLogSql.trim(updateSql.getGeneratedSql()), bindLog, rowCount);
    }
    if (updateSql.isAutoTableMod()) {
      // add the modification info to the TransactionEvent
      // this is used to invalidate cached objects etc
      switch (sqlType) {
        case SQL_INSERT:
          transaction.event().add(tableName, true, false, false);
          break;
        case SQL_UPDATE:
          transaction.event().add(tableName, false, true, false);
          break;
        case SQL_DELETE:
          transaction.event().add(tableName, false, false, true);
          break;
        case SQL_UNKNOWN:
          transaction.markNotQueryOnly();

        default:
          break;
      }
    }
  }

  @Override
  public Object identifier() {
    return bindLog; // CHECKME: Would we leak internal states here?
  }
}
