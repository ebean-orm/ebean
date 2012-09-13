/**
 * Copyright (C) 2009  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
