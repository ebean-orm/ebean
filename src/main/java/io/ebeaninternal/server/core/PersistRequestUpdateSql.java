package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.persist.PersistExecute;

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

  private String description;

  private boolean addBatch;

  /**
   * Create.
   */
  public PersistRequestUpdateSql(SpiEbeanServer server, SpiSqlUpdate sqlUpdate,
                                 SpiTransaction t, PersistExecute persistExecute) {

    super(server, t, persistExecute, sqlUpdate.getLabel());
    this.type = Type.UPDATESQL;
    this.updateSql = sqlUpdate;
    updateSql.reset();
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

  @Override
  public int executeNow() {
    return persistExecute.executeSqlUpdate(this);
  }

  @Override
  public boolean isBatchThisRequest() {
    return addBatch || super.isBatchThisRequest();
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
  public void setType(SqlType sqlType, String tableName, String description) {
    this.sqlType = sqlType;
    this.tableName = tableName;
    this.description = description;
  }

  /**
   * Set the bound values.
   */
  public void setBindLog(String bindLog) {
    this.bindLog = bindLog;
  }

  /**
   * Perform post execute processing.
   */
  @Override
  public void postExecute() {
    if (startNanos > 0) {
      persistExecute.collectSqlUpdate(label, startNanos, rowCount);
    }
    if (transaction.isLogSummary()) {
      String m = description + " table[" + tableName + "] rows[" + rowCount + "] bind[" + bindLog + "]";
      transaction.logSummary(m);
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
