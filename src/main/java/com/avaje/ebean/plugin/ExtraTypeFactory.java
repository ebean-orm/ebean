package com.avaje.ebean.plugin;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.type.ScalarType;

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
