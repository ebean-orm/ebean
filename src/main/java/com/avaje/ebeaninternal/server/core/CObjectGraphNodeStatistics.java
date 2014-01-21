package com.avaje.ebeaninternal.server.core;

import java.util.concurrent.atomic.AtomicLong;

import com.avaje.ebean.meta.*;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebeaninternal.server.util.LongAdder;

/**
 * Helper to collect the query execution statistics for a given node.
 */
public class CObjectGraphNodeStatistics {

  private final ObjectGraphNode node;

  private final LongAdder count = new LongAdder();

  private final LongAdder totalTime = new LongAdder();

  private final LongAdder totalBeans = new LongAdder();

  private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  public CObjectGraphNodeStatistics(ObjectGraphNode node) {
    this.node = node;
  }

  public void add(long beanCount, long exeMicros) {
    count.increment();
    totalTime.add(exeMicros);
    totalBeans.add(beanCount);
  }

  public MetaObjectGraphNodeStats get(boolean reset) {
    if (reset) {
      return new Snapshot(node, startTime.getAndSet(System.currentTimeMillis()), count.sumThenReset(),
          totalTime.sumThenReset(), totalBeans.sumThenReset());
    } else {
      return new Snapshot(node, startTime.get(), count.sum(), totalTime.sum(), totalBeans.sum());
    }
  }

  private static class Snapshot implements MetaObjectGraphNodeStats {

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
