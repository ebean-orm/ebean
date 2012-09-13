package com.avaje.ebeaninternal.api;

import com.avaje.ebean.BackgroundExecutor;

/**
 * Internal Extension to BackgroundExecutor with shutdown.
 */
public interface SpiBackgroundExecutor extends BackgroundExecutor {

	/**
	 * Shutdown any associated thread pools.
	 */
	public void shutdown();
}
