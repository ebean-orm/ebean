package io.ebean.core.type;

import io.avaje.lang.Nullable;
import io.ebean.config.DatabaseConfig;

/**
 * Factory to create ScalarTypeSet.
 */
public interface ScalarTypeSetFactory {

  /**
   * Create the ScalarTypeSet given the config and optional objectMapper.
   */
  @Nullable
  ScalarTypeSet<?> createTypeSet(DatabaseConfig config, @Nullable Object objectMapper);

}
