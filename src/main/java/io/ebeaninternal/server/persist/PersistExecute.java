package io.ebeaninternal.server.persist;

import io.ebean.meta.MetricVisitor;
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

  /**
   * Collect execution metrics for sql update.
   */
  void collectOrmUpdate(String label, long startNanos, int rowCount);

  /**
   * Collect execution metrics for sql update.
   */
  void collectSqlUpdate(String label, long startNanos, int rowCount);

  /**
   * Collect execution metrics for sql callable.
   */
  void collectSqlCall(String label, long startNanos, int rowCount);

  /**
   * Visit the metrics.
   */
  void visitMetrics(MetricVisitor visitor);
}
