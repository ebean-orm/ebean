package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.*;
import com.avaje.ebeaninternal.server.deploy.BeanManager;

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
   * Default for whether to call getGeneratedKeys after batch insert.
   */
  private final boolean defaultBatchGenKeys = true;

  /**
   * Construct this DmlPersistExecute.
   */
  public DefaultPersistExecute(Binder binder, PstmtBatch pstmtBatch, int defaultBatchSize) {

    this.exeOrmUpdate = new ExeOrmUpdate(binder, pstmtBatch);
    this.exeUpdateSql = new ExeUpdateSql(binder, pstmtBatch);
    this.exeCallableSql = new ExeCallableSql(binder, pstmtBatch);
    this.defaultBatchSize = defaultBatchSize;
  }

  public BatchControl createBatchControl(SpiTransaction t) {

    // create a BatchControl and set its defaults
    return new BatchControl(t, defaultBatchSize, defaultBatchGenKeys);
  }

  /**
   * execute the bean insert request.
   */
  public <T> void executeInsertBean(PersistRequestBean<T> request) {

    BeanManager<T> mgr = request.getBeanManager();
    BeanPersister persister = mgr.getBeanPersister();

    BeanPersistController controller = request.getBeanController();
    if (controller == null || controller.preInsert(request)) {
      persister.insert(request);
    }
  }

  /**
   * execute the bean update request.
   */
  public <T> void executeUpdateBean(PersistRequestBean<T> request) {

    BeanManager<T> mgr = request.getBeanManager();
    BeanPersister persister = mgr.getBeanPersister();

    BeanPersistController controller = request.getBeanController();
    if (controller == null || controller.preUpdate(request)) {
      request.postControllerPrepareUpdate();
      persister.update(request);
    }
  }

  /**
   * execute the bean delete request.
   */
  public <T> void executeDeleteBean(PersistRequestBean<T> request) {

    BeanManager<T> mgr = request.getBeanManager();
    BeanPersister persister = mgr.getBeanPersister();

    BeanPersistController controller = request.getBeanController();
    if (controller == null || controller.preDelete(request)) {
      persister.delete(request);
    }
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
