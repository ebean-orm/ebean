package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.meta.MetaQueryPlanOriginCount;
import io.ebean.meta.MetaQueryPlanStatistic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Statistics for a specific query plan that can accumulate.
 */
public final class CQueryPlanStats {

  private final CQueryPlan queryPlan;

  private final LongAdder count = new LongAdder();

  private final LongAdder totalTime = new LongAdder();

  private final LongAdder totalBeans = new LongAdder();

  private final LongAccumulator maxTime = new LongAccumulator(Math::max, Long.MIN_VALUE);

  private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  private long lastQueryTime;

  private final ConcurrentHashMap<ObjectGraphNode, LongAdder> origins;

  /**
   * Construct for a given query plan.
   */
  CQueryPlanStats(CQueryPlan queryPlan, boolean collectQueryOrigins) {

    this.queryPlan = queryPlan;
    this.origins = !collectQueryOrigins ? null : new ConcurrentHashMap<>();
  }

  /**
   * Return true if there are no statistics collected since the last reset.
   */
  public boolean isEmpty() {
    return count.sum() == 0;
  }

  /**
   * Add a query execution to the statistics.
   */
  public void add(long loadedBeanCount, long timeMicros, ObjectGraphNode objectGraphNode) {

    count.increment();
    totalBeans.add(loadedBeanCount);
    totalTime.add(timeMicros);
    maxTime.accumulate(timeMicros);

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

    // Racey but near enough for our purposes as we don't want locks
    count.reset();
    totalBeans.reset();
    totalTime.reset();
    maxTime.reset();
    startTime.set(System.currentTimeMillis());

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

    List<MetaQueryPlanOriginCount> origins = getOrigins(reset);

    // not guaranteed to be consistent due to time gaps between getting each value out of LongAdders but can live with that
    // relative to the cost of making sure count and totalTime etc are all guaranteed to be consistent
    if (reset) {
      return new Snapshot(queryPlan, count.sumThenReset(), totalTime.sumThenReset(), totalBeans.sumThenReset(), maxTime.getThenReset(), startTime.getAndSet(System.currentTimeMillis()), lastQueryTime, origins);
    }
    return new Snapshot(queryPlan, count.sum(), totalTime.sum(), totalBeans.sum(), maxTime.get(), startTime.get(), lastQueryTime, origins);
  }

  /**
   * Return the list/snapshot of the origins and their counter value.
   */
  private List<MetaQueryPlanOriginCount> getOrigins(boolean reset) {
    if (origins == null) {
      return Collections.emptyList();
    }

    List<MetaQueryPlanOriginCount> list = new ArrayList<>(origins.size());

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
  private static class OriginSnapshot implements MetaQueryPlanOriginCount {
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
  public static class Snapshot implements MetaQueryPlanStatistic {

    private final CQueryPlan queryPlan;
    private final long count;
    private final long totalTime;
    private final long totalBeans;
    private final long maxTime;
    private final long startTime;
    private final long lastQueryTime;
    private final List<MetaQueryPlanOriginCount> origins;

    Snapshot(CQueryPlan queryPlan, long count, long totalTime, long totalBeans, long maxTime, long startTime, long lastQueryTime,
             List<MetaQueryPlanOriginCount> origins) {

      this.queryPlan = queryPlan;
      this.count = count;
      this.totalTime = totalTime;
      this.totalBeans = totalBeans;
      this.maxTime = maxTime;
      this.startTime = startTime;
      this.lastQueryTime = lastQueryTime;
      this.origins = origins;
    }

    @Override
    public String toString() {
      ProfileLocation profileLocation = queryPlan.getProfileLocation();
      String loc = (profileLocation == null) ? "" : profileLocation.shortDescription();
      return "location:" + loc + " count:" + count + " time:" + totalTime  + " maxTime:" + maxTime + " beans:" + totalBeans + " sql:" + getSql();
    }

    @Override
    public Class<?> getBeanType() {
      return queryPlan.getBeanType();
    }

    @Override
    public ProfileLocation getProfileLocation() {
      return queryPlan.getProfileLocation();
    }

    @Override
    public long getExecutionCount() {
      return count;
    }

    @Override
    public long getTotalTimeMicros() {
      return totalTime;
    }

    @Override
    public long getTotalLoadedBeans() {
      return totalBeans;
    }

    @Override
    public long getMaxTimeMicros() {
      return maxTime;
    }

    @Override
    public long getCollectionStart() {
      return startTime;
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
    public long getAvgTimeMicros() {
      return count < 1 ? 0 : totalTime / count;
    }

    @Override
    public long getAvgLoadedBeans() {
      return count < 1 ? 0 : totalBeans / count;
    }

    @Override
    public List<MetaQueryPlanOriginCount> getOrigins() {
      return origins;
    }

  }

}
