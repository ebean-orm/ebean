package io.ebeaninternal.api;

import io.ebean.BackgroundExecutor;

/**
 * Internal Extension to BackgroundExecutor with shutdown.
 */
public interface SpiBackgroundExecutor extends BackgroundExecutor {

  /**
   * Shutdown any associated thread pools.
   */
  void shutdown();
}
