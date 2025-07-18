package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiUpdate;
import io.ebeaninternal.api.SpiUpdate.OrmUpdateType;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanManager;
import io.ebeaninternal.server.persist.PersistExecute;

/**
 * Persist request specifically for CallableSql.
 */
public final class PersistRequestOrmUpdate extends PersistRequest {

  private final BeanDescriptor<?> beanDescriptor;
  private final SpiUpdate<?> ormUpdate;
  private int rowCount;
  private String bindLog;

  public PersistRequestOrmUpdate(SpiEbeanServer server, BeanManager<?> mgr, SpiUpdate<?> ormUpdate, SpiTransaction t, PersistExecute persistExecute) {
    super(server, t, persistExecute, ormUpdate.label());
    this.beanDescriptor = mgr.getBeanDescriptor();
    this.ormUpdate = ormUpdate;
  }

  @Override
  public void profile(long offset, int flushCount) {
    profileBase(EVT_ORMUPDATE, offset, beanDescriptor.name(), flushCount);
  }

  public BeanDescriptor<?> descriptor() {
    return beanDescriptor;
  }

  @Override
  public int executeNow() {
    return persistExecute.executeOrmUpdate(this);
  }

  @Override
  public int executeOrQueue() {
    return executeStatement();
  }

  /**
   * Return the UpdateSql.
   */
  public SpiUpdate<?> ormUpdate() {
    return ormUpdate;
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
    OrmUpdateType ormUpdateType = ormUpdate.ormUpdateType();
    if (OrmUpdateType.INSERT != ormUpdateType && !transaction.isAutoPersistUpdates()) {
      beanDescriptor.contextClear(transaction.persistenceContext());
    }
    if (startNanos > 0) {
      persistExecute.collectOrmUpdate(label, startNanos);
    }
    String tableName = ormUpdate.baseTable();
    if (transaction.isLogSummary()) {
      transaction.logSummary("{0} table[{1}] rows[{2}] bind[{3}]", ormUpdateType, tableName, rowCount, bindLog);
    }
    if (ormUpdate.isNotifyCache()) {
      // add the modification info to the TransactionEvent
      // this is used to invalidate cached objects etc
      switch (ormUpdateType) {
        case INSERT:
          transaction.event().add(tableName, true, false, false);
          break;
        case UPDATE:
          transaction.event().add(tableName, false, true, false);
          break;
        case DELETE:
          transaction.event().add(tableName, false, false, true);
          break;
        default:
          break;
      }
    }
  }

  @Override
  public Object identifier() {
    return bindLog;
  }
}
