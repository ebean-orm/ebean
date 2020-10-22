package io.ebeaninternal.api;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.type.GeoTypeBinder;

/**
 * Provider of Geometry type binder support.
 */
public interface GeoTypeProvider {

  /**
   * Create a binder for binding geometry types.
   */
  GeoTypeBinder createBinder(DatabaseConfig config);
}
