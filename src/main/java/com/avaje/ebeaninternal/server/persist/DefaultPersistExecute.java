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
  public <T> int executeDeleteBean(PersistRequestBean<T> request) {

    BeanManager<T> mgr = request.getBeanManager();
    BeanPersister persister = mgr.getBeanPersister();

    BeanPersistController controller = request.getBeanController();
    if (controller == null || controller.preDelete(request)) {
      return persister.delete(request);
    }
    // delete handled by the BeanController so return 0
    return 0;
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
