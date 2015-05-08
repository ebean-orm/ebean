package com.avaje.ebeaninternal.server.core;

import java.util.concurrent.TimeUnit;

import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.server.lib.DaemonScheduleThreadPool;
import com.avaje.ebeaninternal.server.lib.DaemonThreadPool;

/**
 * The default implementation of the BackgroundExecutor.
 */
public class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

	private final DaemonThreadPool pool;
	
	private final DaemonScheduleThreadPool schedulePool;

  /**
   * Construct the default implementation of BackgroundExecutor.
   *
   * @param corePoolSize
   *            the core size of the thread pool.
   * @param maximumPoolSize
   *            the maximum pool size before jobs are queued
   * @param keepAliveSecs
   *            the time in seconds idle threads are keep alive
   * @param shutdownWaitSeconds
   *            the time in seconds allowed for the pool to shutdown nicely.
   *            After this the pool is forced to shutdown.
   */
	public DefaultBackgroundExecutor(int schedulePoolSize, int corePoolSize, int maximumPoolSize, long keepAliveSecs,int shutdownWaitSeconds, String namePrefix) {
		this.pool = new DaemonThreadPool(corePoolSize, maximumPoolSize, keepAliveSecs, shutdownWaitSeconds, namePrefix);
		this.schedulePool = new DaemonScheduleThreadPool(schedulePoolSize, shutdownWaitSeconds, namePrefix+"-periodic-");
	}

	/**
	 * Execute a Runnable using a background thread.
	 */
	public void execute(Runnable r) {
		pool.execute(r);
	}

	public void executePeriodically(Runnable r, long delay, TimeUnit unit) {
		schedulePool.scheduleWithFixedDelay(r, delay, delay, unit);
	}

	public void shutdown() {
		pool.shutdown();
		schedulePool.shutdown();
	}
	
}
