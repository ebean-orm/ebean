package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaOrmQueryOrigin;
import io.ebean.meta.MetricType;
import io.ebeaninternal.metric.TimedMetric;
import io.ebeaninternal.metric.TimedMetricStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Statistics for a specific query plan that can accumulate.
 */
public final class CQueryPlanStats {

  private final CQueryPlan queryPlan;

  private final TimedMetric timedMetric;

  private long lastQueryTime;

  private final ConcurrentHashMap<ObjectGraphNode, LongAdder> origins;

  /**
   * Construct for a given query plan.
   */
  CQueryPlanStats(CQueryPlan queryPlan, boolean collectQueryOrigins) {
    this.queryPlan = queryPlan;
    this.origins = !collectQueryOrigins ? null : new ConcurrentHashMap<>();
    this.timedMetric = queryPlan.createTimedMetric();
  }

  /**
   * Return true if there are no statistics collected since the last reset.
   */
  public boolean isEmpty() {
    return timedMetric.isEmpty();
  }

  /**
   * Add a query execution to the statistics.
   */
  public void add(long loadedBeanCount, long timeMicros, ObjectGraphNode objectGraphNode) {

    timedMetric.add(timeMicros, loadedBeanCount);

    // not safe but should be atomic
    lastQueryTime = System.currentTimeMillis();

    if (origins != null && objectGraphNode != null) {
      // Maintain the origin points this query fires from
      // with a simple counter
      LongAdder counter = origins.get(objectGraphNode);
      if (counter == null) {
        // race condition - we can miss counters here but going
        // to live with that. Don't want to lock/synchronize etc
        counter = new LongAdder();
        origins.put(objectGraphNode, counter);
      }
      counter.increment();
    }
  }

  /**
   * Reset the internal statistics counters.
   */
  public void reset() {
    timedMetric.reset();
    if (origins != null) {
      for (LongAdder counter : origins.values()) {
        counter.reset();
      }
    }
  }

  /**
   * Return the last time this query was executed.
   */
  long getLastQueryTime() {
    return lastQueryTime;
  }

  /**
   * Return a Snapshot of the query execution statistics potentially resetting the internal counters.
   */
  Snapshot getSnapshot(boolean reset) {

    TimedMetricStats collect = timedMetric.collect(reset);
    List<MetaOrmQueryOrigin> origins = getOrigins(reset);
    return new Snapshot(queryPlan, collect, lastQueryTime, origins);
  }

  /**
   * Return the list/snapshot of the origins and their counter value.
   */
  private List<MetaOrmQueryOrigin> getOrigins(boolean reset) {
    if (origins == null) {
      return Collections.emptyList();
    }

    List<MetaOrmQueryOrigin> list = new ArrayList<>(origins.size());

    for (Entry<ObjectGraphNode, LongAdder> entry : origins.entrySet()) {
      if (reset) {
        list.add(new OriginSnapshot(entry.getKey(), entry.getValue().sumThenReset()));
      } else {
        list.add(new OriginSnapshot(entry.getKey(), entry.getValue().sum()));
      }
    }
    return list;
  }

  /**
   * Snapshot of the origin ObjectGraphNode and counter value.
   */
  private static class OriginSnapshot implements MetaOrmQueryOrigin {
    private final ObjectGraphNode objectGraphNode;
    private final long count;

    OriginSnapshot(ObjectGraphNode objectGraphNode, long count) {
      this.objectGraphNode = objectGraphNode;
      this.count = count;
    }

    @Override
    public String toString() {
      return "node[" + objectGraphNode + "] count[" + count + "]";
    }

    @Override
    public ObjectGraphNode getObjectGraphNode() {
      return objectGraphNode;
    }

    @Override
    public long getCount() {
      return count;
    }
  }

  /**
   * A snapshot of the current statistics for a query plan.
   */
  static class Snapshot implements MetaOrmQueryMetric {

    private final CQueryPlan queryPlan;
    private final TimedMetricStats metrics;
    private final long lastQueryTime;
    private final List<MetaOrmQueryOrigin> origins;

    Snapshot(CQueryPlan queryPlan, TimedMetricStats metrics, long lastQueryTime, List<MetaOrmQueryOrigin> origins) {
      this.queryPlan = queryPlan;
      this.metrics = metrics;
      this.lastQueryTime = lastQueryTime;
      this.origins = origins;
    }

    @Override
    public String toString() {
      return "location:" + getLocation() + " metrics:" + metrics + " sql:" + getSql();
    }

    @Override
    public MetricType getMetricType() {
      return MetricType.ORM;
    }

    @Override
    public Class<?> getType() {
      return queryPlan.getBeanType();
    }

    @Override
    public String getLabel() {
      return queryPlan.getLabel();
    }

    @Override
    public String getName() {
      return queryPlan.getLabel();
    }

    @Override
    public String getLocation() {
      return queryPlan.getLocation();
    }

    @Override
    public ProfileLocation getProfileLocation() {
      return queryPlan.getProfileLocation();
    }

    @Override
    public long getBeanCount() {
      return metrics.getBeanCount();
    }

    @Override
    public long getCount() {
      return metrics.getCount();
    }

    @Override
    public long getTotal() {
      return metrics.getTotal();
    }

    @Override
    public long getMax() {
      return metrics.getMax();
    }

    @Override
    public long getMean() {
      return metrics.getMean();
    }

    @Override
    public long getStartTime() {
      return metrics.getStartTime();
    }

    @Override
    public long getLastQueryTime() {
      return lastQueryTime;
    }

    @Override
    public boolean isAutoTuned() {
      return queryPlan.isAutoTuned();
    }

    @Override
    public String getQueryPlanHash() {
      return queryPlan.getPlanKey().toString();
    }

    @Override
    public String getSql() {
      return queryPlan.getSql();
    }

    @Override
    public List<MetaOrmQueryOrigin> getOrigins() {
      return origins;
    }

  }

}
