package io.ebean.core.type;

import io.ebean.DatabaseBuilder;

import java.util.List;

/**
 * A factory that provides extra types to Ebean.
 */
public interface ExtraTypeFactory {

  /**
   * Provide extra types to Ebean.
   */
  List<? extends ScalarType<?>> createTypes(DatabaseBuilder.Settings config, Object objectMapper);
}
