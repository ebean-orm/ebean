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
import com.avaje.ebeaninternal.server.lib.DaemonThreadPool;

/**
 * The default implementation of the BackgroundExecutor.
 * 
 * @author rbygrave
 */
public class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

	private final DaemonThreadPool pool;
	
	private final DaemonScheduleThreadPool schedulePool;

	/**
	 * Construct the default implementation of BackgroundExecutor.
	 * 
	 * @param mainPoolSize
	 *            the core size of the thread pool.
	 * @param keepAliveSecs
	 *            the time in seconds idle threads are keep alive
	 * @param shutdownWaitSeconds
	 *            the time in seconds allowed for the pool to shutdown nicely.
	 *            After this the pool is forced to shutdown.
	 */
	public DefaultBackgroundExecutor(int mainPoolSize, int schedulePoolSize, long keepAliveSecs,int shutdownWaitSeconds, String namePrefix) {
		this.pool = new DaemonThreadPool(mainPoolSize, keepAliveSecs, shutdownWaitSeconds, namePrefix);
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
