package io.ebeaninternal.api;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.type.ScalarType;

import java.util.List;

/**
 * A factory that provides extra types to Ebean.
 */
public interface ExtraTypeFactory {

  /**
   * Provide extra types to Ebean.
   */
  List<? extends ScalarType<?>> createTypes(DatabaseConfig config, Object objectMapper);
}
