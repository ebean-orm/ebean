package io.ebean.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple MetricVisitor that can collect the desired metrics into lists.
 */
public class BasicMetricVisitor extends AbstractMetricVisitor implements ServerMetrics {

  private final List<MetaTimedMetric> timed = new ArrayList<>();
  private final List<MetaQueryMetric> dtoQuery = new ArrayList<>();
  private final List<MetaOrmQueryMetric> ormQuery = new ArrayList<>();
  private final List<MetaCountMetric> countMetrics = new ArrayList<>();

  /**
   * Construct to reset and collect everything.
   */
  public BasicMetricVisitor() {
    super(true, true, true, true);
  }

  /**
   * Construct specifying reset and what to collect.
   */
  public BasicMetricVisitor(boolean reset, boolean collectTransactionMetrics, boolean collectQueryMetrics, boolean collectL2Metrics) {
    super(reset, collectTransactionMetrics, collectQueryMetrics, collectL2Metrics);
  }

  /**
   * Return timed metrics for Transactions, labelled SqlQuery, labelled SqlUpdate.
   */
  @Override
  public List<MetaTimedMetric> getTimedMetrics() {
    return timed;
  }

  /**
   * Return the DTO query metrics.
   */
  @Override
  public List<MetaQueryMetric> getDtoQueryMetrics() {
    return dtoQuery;
  }

  /**
   * Return the ORM query metrics.
   */
  @Override
  public List<MetaOrmQueryMetric> getOrmQueryMetrics() {
    return ormQuery;
  }

  @Override
  public List<MetaCountMetric> getCountMetrics() {
    return countMetrics;
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

  @Override
  public void visitCount(MetaCountMetric metric) {
    countMetrics.add(metric);
  }
}
