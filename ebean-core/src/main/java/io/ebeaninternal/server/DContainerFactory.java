package io.ebeaninternal.server;

import io.ebean.config.ContainerConfig;
import io.ebean.service.SpiContainer;
import io.ebean.service.SpiContainerFactory;
import io.ebeaninternal.server.core.DefaultContainer;

/**
 * Default container factory found via service loader.
 */
public class DContainerFactory implements SpiContainerFactory {

  @Override
  public SpiContainer create(ContainerConfig containerConfig) {
    return new DefaultContainer(containerConfig);
  }
}
