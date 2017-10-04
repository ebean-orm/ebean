package io.ebeaninternal.server.lib;

import io.ebean.service.SpiContainerShutdown;

/**
 * Default container shutdown implementation.
 */
public class DContainerShutdown implements SpiContainerShutdown {

  @Override
  public void shutdown() {
    ShutdownManager.shutdown();
  }
}
