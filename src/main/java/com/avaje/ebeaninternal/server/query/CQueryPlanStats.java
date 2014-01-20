
package com.avaje.ebeaninternal.server.query;

import java.util.concurrent.atomic.AtomicLong;

import com.avaje.ebean.meta.MetaBeanQueryPlanStatistic;
import com.avaje.ebeaninternal.server.util.LongAdder;

/**
 * Statistics for a specific query plan that can accumulate. 
 */
public final class CQueryPlanStats {

  private final CQueryPlan queryPlan;
  
  private final LongAdder count = new LongAdder();

  private final LongAdder totalTime = new LongAdder();

  private final LongAdder totalBeans = new LongAdder();

  private final AtomicLong maxTime = new AtomicLong();

  private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  private long lastQueryTime;

	
	public CQueryPlanStats(CQueryPlan queryPlan) {
	  this.queryPlan = queryPlan;
	}

	public void add(long loadedBeanCount, long timeMicros) {
		count.increment();
		totalBeans.add(loadedBeanCount);
		totalTime.add(timeMicros);
		if (timeMicros > maxTime.get()) {
		  // effectively a high water mark
		  maxTime.set(timeMicros);
		}
    lastQueryTime = System.currentTimeMillis();
	}
	
	public void reset() {
	  count.reset();
	  totalBeans.reset();
	  totalTime.reset();
	  maxTime.set(0);
	  startTime.set(System.currentTimeMillis());
	}
	
	public long getLastQueryTime() {
		return lastQueryTime;
	}
	
	public Snapshot getSnapshot(boolean reset) {
	  // not guaranteed to be consistent  - time gaps between getting each value  
	  if (reset) {
	    return new Snapshot(queryPlan, count.sumThenReset(), totalTime.sumThenReset(), totalBeans.sumThenReset(), maxTime.getAndSet(0), startTime.getAndSet(System.currentTimeMillis()), lastQueryTime);
	  }
    return new Snapshot(queryPlan, count.sum(), totalTime.sum(), totalBeans.sum(), maxTime.get(), startTime.get(), lastQueryTime);
	}

	/**
	 * A snapshot of the current statistics for a query plan.
	 */
	public static class Snapshot implements MetaBeanQueryPlanStatistic {
	  
	  private final CQueryPlan queryPlan;
	  private final long count;
	  private final long totalTime;
	  private final long totalBeans;
	  private final long maxTime;  
	  private final long startTime;
	  private final long lastQueryTime;
    
	  public Snapshot(CQueryPlan queryPlan, long count, long totalTime, long totalBeans, long maxTime, long startTime, long lastQueryTime) {
      super();
      this.queryPlan = queryPlan;
      this.count = count;
      this.totalTime = totalTime;
      this.totalBeans = totalBeans;
      this.maxTime = maxTime;
      this.startTime = startTime;
      this.lastQueryTime = lastQueryTime;
    }
	  	  
	  public String toString() {
	    return queryPlan+" count:"+count+" time:"+totalTime+" maxTime:"+maxTime+" beans:"+totalBeans+" start:"+startTime+" lastQuery:"+lastQueryTime;
	  }
	  
    @Override
    public Class<?> getBeanType() {
      return queryPlan.getBeanType();
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
    public boolean isAutofetchTuned() {
      return queryPlan.isAutofetchTuned();
    }


    @Override
    public String getQueryPlanHash() {
      return queryPlan.getHash().toString();
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
	}
	
}