package io.ebean.service;

import io.ebean.ImmutableCacheBuilder;

/**
 * Factory for creating immutable cache builders.
 */
public interface SpiImmutableCacheFactory extends BootstrapService {

  /**
   * Return a new builder for the given immutable bean type.
   */
  <T> ImmutableCacheBuilder<T> builder(Class<T> type);
}
