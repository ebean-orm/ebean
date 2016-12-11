package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestCallableSql;
import io.ebeaninternal.server.core.PersistRequestOrmUpdate;
import io.ebeaninternal.server.core.PersistRequestUpdateSql;

/**
 * The actual execution of persist requests.
 * <p>
 * A Persister 'front-ends' this object and handles the
 * batching, cascading, concurrency mode detection etc.
 * </p>
 */
public interface PersistExecute {

  /**
   * Create a BatchControl for the current transaction.
   */
  BatchControl createBatchControl(SpiTransaction t);

  /**
   * Execute a Update.
   */
  int executeOrmUpdate(PersistRequestOrmUpdate request);

  /**
   * Execute a CallableSql.
   */
  int executeSqlCallable(PersistRequestCallableSql request);

  /**
   * Execute a UpdateSql.
   */
  int executeSqlUpdate(PersistRequestUpdateSql request);

}
