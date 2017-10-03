package io.ebean.plugin;

import io.ebean.config.ServerConfig;
import io.ebean.databind.ScalarType;

import java.util.List;

/**
 * A factory that provides extra types to Ebean.
 */
public interface ExtraTypeFactory {

  /**
   * Provide extra types to Ebean.
   */
  List<? extends ScalarType<?>> createTypes(ServerConfig config, Object objectMapper);
}
