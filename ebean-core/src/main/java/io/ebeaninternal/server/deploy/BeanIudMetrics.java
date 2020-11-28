package io.ebeaninternal.server.deploy;

import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebeaninternal.server.core.PersistRequest;

/**
 * Metrics for ORM Insert Update and Delete for a given bean type.
 */
class BeanIudMetrics {

  private final TimedMetric insert;
  private final TimedMetric update;
  private final TimedMetric delete;
  private final TimedMetric insertBatch;
  private final TimedMetric updateBatch;
  private final TimedMetric deleteBatch;

  /**
   * Create for a given bean type.
   */
  BeanIudMetrics(String beanShortName) {

    MetricFactory metricFactory = MetricFactory.get();
    String prefix = "iud." + beanShortName;
    this.insert = metricFactory.createTimedMetric(prefix + ".insert");
    this.update = metricFactory.createTimedMetric(prefix + ".update");
    this.delete = metricFactory.createTimedMetric(prefix + ".delete");
    this.insertBatch = metricFactory.createTimedMetric(prefix + ".insertBatch");
    this.updateBatch = metricFactory.createTimedMetric(prefix + ".updateBatch");
    this.deleteBatch = metricFactory.createTimedMetric(prefix + ".deleteBatch");
  }

  /**
   * Add batch persist metric.
   */
  void addBatch(PersistRequest.Type type, long startNanos, int batch) {
    switch (type) {
      case INSERT:
        insertBatch.addBatchSince(startNanos, batch);
        break;
      case UPDATE:
      case DELETE_SOFT:
        updateBatch.addBatchSince(startNanos, batch);
        break;
      case DELETE:
      case DELETE_PERMANENT:
        deleteBatch.addBatchSince(startNanos, batch);
        break;
    }
  }

  /**
   * Add Non-batch persist metric.
   */
  void addNoBatch(PersistRequest.Type type, long startNanos) {
    switch (type) {
      case INSERT:
        insert.addSinceNanos(startNanos);
        break;
      case UPDATE:
      case DELETE_SOFT:
        update.addSinceNanos(startNanos);
        break;
      case DELETE:
      case DELETE_PERMANENT:
        delete.addSinceNanos(startNanos);
        break;
    }
  }

  void visit(MetricVisitor visitor) {
    insert.visit(visitor);
    update.visit(visitor);
    delete.visit(visitor);
    insertBatch.visit(visitor);
    updateBatch.visit(visitor);
    deleteBatch.visit(visitor);
  }
}
