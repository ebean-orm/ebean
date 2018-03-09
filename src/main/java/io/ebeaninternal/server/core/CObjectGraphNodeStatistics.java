package io.ebeaninternal.server.core;

import io.ebean.bean.ObjectGraphNode;
import io.ebean.meta.MetaOrmQueryNode;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Helper to collect the query execution statistics for a given node.
 */
public class CObjectGraphNodeStatistics {

  private final ObjectGraphNode node;

  private final LongAdder count = new LongAdder();

  private final LongAdder totalTime = new LongAdder();

  private final LongAdder totalBeans = new LongAdder();

  private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  CObjectGraphNodeStatistics(ObjectGraphNode node) {
    this.node = node;
  }

  public boolean isEmpty() {
    return count.sum() == 0;
  }

  public void add(long beanCount, long exeMicros) {
    count.increment();
    totalTime.add(exeMicros);
    totalBeans.add(beanCount);
  }

  public MetaOrmQueryNode get(boolean reset) {
    if (reset) {
      return new Snapshot(node, startTime.getAndSet(System.currentTimeMillis()), count.sumThenReset(),
        totalTime.sumThenReset(), totalBeans.sumThenReset());
    } else {
      return new Snapshot(node, startTime.get(), count.sum(), totalTime.sum(), totalBeans.sum());
    }
  }

  private static class Snapshot implements MetaOrmQueryNode {

    private final ObjectGraphNode node;
    private final long startTime;
    private final long count;
    private final long totalTime;
    private final long totalBeans;

    public Snapshot(ObjectGraphNode node, long startTime, long count, long totalTime, long totalBeans) {
      this.node = node;
      this.startTime = startTime;
      this.count = count;
      this.totalTime = totalTime;
      this.totalBeans = totalBeans;
    }

    @Override
    public String toString() {
      return node + " count[" + count + "] time[" + totalTime + "] beans[" + totalBeans + "]";
    }

    @Override
    public ObjectGraphNode getNode() {
      return node;
    }

    @Override
    public long getStartTime() {
      return startTime;
    }

    @Override
    public long getCount() {
      return count;
    }

    @Override
    public long getTotalTime() {
      return totalTime;
    }

    @Override
    public long getTotalBeans() {
      return totalBeans;
    }
  }

}
