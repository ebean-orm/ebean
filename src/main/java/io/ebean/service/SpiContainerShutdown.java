package io.ebean.service;

/**
 * Provides shutdown of the entire container.
 */
public interface SpiContainerShutdown {

  /**
   * Shutdown the entire container - all EbeanServer instances.
   */
  void shutdown();
}
