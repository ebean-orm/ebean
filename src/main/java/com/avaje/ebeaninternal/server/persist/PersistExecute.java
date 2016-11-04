package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestUpdateSql;

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
