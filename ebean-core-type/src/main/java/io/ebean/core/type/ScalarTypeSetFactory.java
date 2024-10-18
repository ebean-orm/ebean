package io.ebean.core.type;

import org.jspecify.annotations.Nullable;
import io.ebean.DatabaseBuilder;

/**
 * Factory to create ScalarTypeSet.
 */
public interface ScalarTypeSetFactory {

  /**
   * Create the ScalarTypeSet given the config and optional objectMapper.
   */
  @Nullable
  ScalarTypeSet<?> createTypeSet(DatabaseBuilder.Settings config, @Nullable Object objectMapper);

}
