package com.avaje.ebeaninternal.server.core;

import java.util.concurrent.TimeUnit;

import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.server.lib.DaemonScheduleThreadPool;
import com.avaje.ebeaninternal.server.lib.thread.ThreadPool;

/**
 * BackgroundExecutor using my traditional ThreadPool that will grow and trim.
 * 
 * @author rbygrave
 */
public class TraditionalBackgroundExecutor implements SpiBackgroundExecutor {

	private final ThreadPool pool;
	
	private final DaemonScheduleThreadPool schedulePool;

	/**
	 * Construct the default implementation of BackgroundExecutor.
	 */
	public TraditionalBackgroundExecutor(ThreadPool pool, int schedulePoolSize, int shutdownWaitSeconds, String namePrefix) {
		this.pool = pool;
		this.schedulePool = new DaemonScheduleThreadPool(schedulePoolSize, shutdownWaitSeconds, namePrefix+"-periodic-");
	}

	/**
	 * Execute a Runnable using a background thread.
	 */
	public void execute(Runnable r) {
	    pool.assign(r, true);
	}

	public void executePeriodically(Runnable r, long delay, TimeUnit unit) {
		schedulePool.scheduleWithFixedDelay(r, delay, delay, unit);
	}

	public void shutdown() {
		// the pool is shutdown automatically by the ThreadPoolManager
		schedulePool.shutdown();
	}
	
}
