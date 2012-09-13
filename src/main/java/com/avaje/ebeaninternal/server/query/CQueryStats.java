
package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.meta.MetaQueryStatistic;

/**
 * Statistics for query plan that can accumulate. 
 */
public final class CQueryStats {

	private final int count;

	private final int totalLoadedBeanCount;

	private final int totalTimeMicros;

	private final long startCollecting;
	
	private final long lastQueryTime;
	
	public CQueryStats() {
		count = 0;
		totalLoadedBeanCount = 0;
		totalTimeMicros = 0;
		startCollecting = System.currentTimeMillis();
		lastQueryTime = 0;
	}

	/**
	 * Accumulate/Increment the statistics based on the previous statistics.
	 */
	public CQueryStats(CQueryStats previous, int loadedBeanCount, int timeMicros) {
		count = previous.count + 1;
		totalLoadedBeanCount = previous.totalLoadedBeanCount + loadedBeanCount;
		totalTimeMicros = previous.totalTimeMicros + timeMicros;
		startCollecting = previous.startCollecting;
		lastQueryTime = System.currentTimeMillis();
	}

	public CQueryStats add(int loadedBeanCount, int timeMicros) {
		return new CQueryStats(this, loadedBeanCount, timeMicros);
	}

	public int getCount() {
		return count;
	}

	public int getAverageTimeMicros() {
		if (count == 0) {
			return 0;
		} else {
			return totalTimeMicros / count;
		}
	}
	
	public int getTotalLoadedBeanCount() {
		return totalLoadedBeanCount;
	}

	public int getTotalTimeMicros() {
		return totalTimeMicros;
	}

	public long getStartCollecting() {
		return startCollecting;
	}
	
	public long getLastQueryTime() {
		return lastQueryTime;
	}

	public MetaQueryStatistic createMetaQueryStatistic(String beanName, CQueryPlan qp) {
		return new MetaQueryStatistic(qp.isAutofetchTuned(), beanName, qp.getHash(),
			qp.getSql(), count, totalLoadedBeanCount, totalTimeMicros, startCollecting, lastQueryTime);
	}

}