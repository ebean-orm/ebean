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

  /**
   * Create.
   */
  public PersistRequestOrmUpdate(SpiEbeanServer server, BeanManager<?> mgr, SpiUpdate<?> ormUpdate,
                                 SpiTransaction t, PersistExecute persistExecute) {

    super(server, t, persistExecute, ormUpdate.getLabel());
    this.beanDescriptor = mgr.getBeanDescriptor();
    this.ormUpdate = ormUpdate;
  }

  @Override
  public void profile(long offset, int flushCount) {
    profileBase(EVT_ORMUPDATE, offset, beanDescriptor.getProfileId(), flushCount);
  }

  public BeanDescriptor<?> getBeanDescriptor() {
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
  public SpiUpdate<?> getOrmUpdate() {
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
    if (startNanos > 0) {
      persistExecute.collectOrmUpdate(label, startNanos, rowCount);
    }
    OrmUpdateType ormUpdateType = ormUpdate.getOrmUpdateType();
    String tableName = ormUpdate.getBaseTable();

    if (transaction.isLogSummary()) {
      String m = ormUpdateType + " table[" + tableName + "] rows[" + rowCount + "] bind[" + bindLog + "]";
      transaction.logSummary(m);
    }

    if (ormUpdate.isNotifyCache()) {

      // add the modification info to the TransactionEvent
      // this is used to invalidate cached objects etc
      switch (ormUpdateType) {
        case INSERT:
          transaction.getEvent().add(tableName, true, false, false);
          break;
        case UPDATE:
          transaction.getEvent().add(tableName, false, true, false);
          break;
        case DELETE:
          transaction.getEvent().add(tableName, false, false, true);
          break;
        default:
          break;
      }
    }
  }

}
