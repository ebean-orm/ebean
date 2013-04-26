package com.avaje.ebeaninternal.server.core;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.server.lib.DaemonScheduleThreadPool;
import com.avaje.ebeaninternal.server.lib.thread.ThreadPool;

/**
 * BackgroundExecutor using my traditional ThreadPool that will grow and trim.
 * 
 * @author rbygrave
 */
public class TraditionalBackgroundExecutor implements SpiBackgroundExecutor {

  private static Logger logger = LoggerFactory.getLogger(TraditionalBackgroundExecutor.class);
  
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
	  if (logger.isDebugEnabled()) {
	    logger.debug("Registering for executePeriodically {} delay:{} {}",r, delay, unit);
	  }
		schedulePool.scheduleWithFixedDelay(r, delay, delay, unit);
	}

	public void shutdown() {
	  if (logger.isDebugEnabled()) {
	    logger.debug("Shutting down");
	  }
	  pool.shutdown();
		schedulePool.shutdown();
	}
	
}
