package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestCallableSql;
import io.ebeaninternal.server.core.PersistRequestOrmUpdate;
import io.ebeaninternal.server.core.PersistRequestUpdateSql;

/**
 * Default PersistExecute implementation using DML statements.
 * <p>
 * Supports the use of PreparedStatement batching.
 * </p>
 */
final class DefaultPersistExecute implements PersistExecute {

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
  DefaultPersistExecute(Binder binder, int defaultBatchSize) {

    this.exeOrmUpdate = new ExeOrmUpdate(binder);
    this.exeUpdateSql = new ExeUpdateSql(binder);
    this.exeCallableSql = new ExeCallableSql(binder);
    this.defaultBatchSize = defaultBatchSize;
  }

  @Override
  public BatchControl createBatchControl(SpiTransaction t) {

    // create a BatchControl and set its defaults
    return new BatchControl(t, defaultBatchSize, true);
  }

  /**
   * Execute the updateSqlRequest
   */
  @Override
  public int executeOrmUpdate(PersistRequestOrmUpdate request) {
    return exeOrmUpdate.execute(request);
  }

  /**
   * Execute the updateSqlRequest
   */
  @Override
  public int executeSqlUpdate(PersistRequestUpdateSql request) {
    return exeUpdateSql.execute(request);
  }

  /**
   * Execute the CallableSqlRequest.
   */
  @Override
  public int executeSqlCallable(PersistRequestCallableSql request) {
    return exeCallableSql.execute(request);
  }

}
