package io.ebean.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple MetricVisitor that can collect the desired metrics into lists.
 */
public class BasicMetricVisitor extends AbstractMetricVisitor {

  private final List<MetaTimedMetric> timed = new ArrayList<>();
  private final List<MetaQueryMetric> dtoQuery = new ArrayList<>();
  private final List<MetaOrmQueryMetric> ormQuery = new ArrayList<>();

  /**
   * Construct to reset and collect everything.
   */
  public BasicMetricVisitor() {
    super(true, true, true);
  }

  /**
   * Construct specifying reset and what to collect.
   */
  public BasicMetricVisitor(boolean reset, boolean collectTransactionMetrics, boolean collectQueryMetrics) {
    super(reset, collectTransactionMetrics, collectQueryMetrics);
  }

  /**
   * Return timed metrics for Transactions, labelled SqlQuery, labelled SqlUpdate.
   */
  public List<MetaTimedMetric> getTimedMetrics() {
    return timed;
  }

  /**
   * Return the DTO query metrics.
   */
  public List<MetaQueryMetric> getDtoQueryMetrics() {
    return dtoQuery;
  }

  /**
   * Return the ORM query metrics.
   */
  public List<MetaOrmQueryMetric> getOrmQueryMetrics() {
    return ormQuery;
  }

  @Override
  public void visitTimed(MetaTimedMetric metric) {
    timed.add(metric);
  }

  @Override
  public void visitQuery(MetaQueryMetric metric) {
    dtoQuery.add(metric);
  }

  @Override
  public void visitOrmQuery(MetaOrmQueryMetric metric) {
    ormQuery.add(metric);
  }
}
