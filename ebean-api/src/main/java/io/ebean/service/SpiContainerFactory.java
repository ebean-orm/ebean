package io.ebean.service;

import io.ebean.config.ContainerConfig;

/**
 * Provides shutdown of the entire container.
 */
public interface SpiContainerFactory {

  /**
   * Create the Container that builds EbeanServer instances.
   */
  SpiContainer create(ContainerConfig containerConfig);
}
