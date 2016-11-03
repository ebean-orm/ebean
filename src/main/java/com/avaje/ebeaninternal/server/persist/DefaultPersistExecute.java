package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestUpdateSql;

/**
 * Default PersistExecute implementation using DML statements.
 * <p>
 * Supports the use of PreparedStatement batching.
 * </p>
 */
public final class DefaultPersistExecute implements PersistExecute {

  private final ExeCallableSql exeCallableSql;

  private final ExeUpdateSql exeUpdateSql;

  private final ExeOrmUpdate exeOrmUpdate;

  /**
   * The default batch size.
   */
  private final int defaultBatchSize;

  /**
   * Construct this DmlPersistExecute.
   */
  public DefaultPersistExecute(Binder binder, int defaultBatchSize) {

    this.exeOrmUpdate = new ExeOrmUpdate(binder);
    this.exeUpdateSql = new ExeUpdateSql(binder);
    this.exeCallableSql = new ExeCallableSql(binder);
    this.defaultBatchSize = defaultBatchSize;
  }

  public BatchControl createBatchControl(SpiTransaction t) {

    // create a BatchControl and set its defaults
    return new BatchControl(t, defaultBatchSize, true);
  }

  /**
   * execute the bean insert request.
   */
  public <T> void executeInsertBean(PersistRequestBean<T> request) {
    request.executeInsert();
  }

  /**
   * execute the bean update request.
   */
  public <T> void executeUpdateBean(PersistRequestBean<T> request) {
    request.executeUpdate();
  }

  /**
   * execute the bean delete request.
   */
  public <T> int executeDeleteBean(PersistRequestBean<T> request) {
    return request.executeDelete();
  }

  @Override
  public <T> void executeSoftDeleteBean(PersistRequestBean<T> request) {
    request.executeSoftDelete();
  }

  /**
   * Execute the updateSqlRequest
   */
  public int executeOrmUpdate(PersistRequestOrmUpdate request) {
    return exeOrmUpdate.execute(request);
  }

  /**
   * Execute the updateSqlRequest
   */
  public int executeSqlUpdate(PersistRequestUpdateSql request) {
    return exeUpdateSql.execute(request);
  }

  /**
   * Execute the CallableSqlRequest.
   */
  public int executeSqlCallable(PersistRequestCallableSql request) {
    return exeCallableSql.execute(request);
  }

}
