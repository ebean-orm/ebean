package io.ebeaninternal.server.persist;

import io.ebean.meta.MetricType;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.TimedMetricMap;
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

  private final TimedMetricMap ormUpdateMetric;

  private final TimedMetricMap sqlUpdateMetric;

  private final TimedMetricMap sqlCallMetric;

  /**
   * Construct this DmlPersistExecute.
   */
  DefaultPersistExecute(Binder binder, int defaultBatchSize) {
    this.exeOrmUpdate = new ExeOrmUpdate(binder);
    this.exeUpdateSql = new ExeUpdateSql(binder);
    this.exeCallableSql = new ExeCallableSql(binder);
    this.defaultBatchSize = defaultBatchSize;
    this.ormUpdateMetric = MetricFactory.get().createTimedMetricMap(MetricType.SQL, "orm.update.");
    this.sqlUpdateMetric = MetricFactory.get().createTimedMetricMap(MetricType.SQL, "sql.update.");
    this.sqlCallMetric = MetricFactory.get().createTimedMetricMap(MetricType.SQL, "sql.call.");
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    ormUpdateMetric.visit(visitor);
    sqlUpdateMetric.visit(visitor);
    sqlCallMetric.visit(visitor);
  }

  @Override
  public void collectOrmUpdate(String label, long startNanos, int rowCount) {
    long exeMicros = (System.nanoTime() - startNanos) / 1000L;
    ormUpdateMetric.add(label, exeMicros, rowCount);
  }

  @Override
  public void collectSqlUpdate(String label, long startNanos, int rowCount) {
    long exeMicros = (System.nanoTime() - startNanos) / 1000L;
    sqlUpdateMetric.add(label, exeMicros, rowCount);
  }

  @Override
  public void collectSqlCall(String label, long startNanos, int rowCount) {
    long exeMicros = (System.nanoTime() - startNanos) / 1000L;
    sqlCallMetric.add(label, exeMicros, rowCount);
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
