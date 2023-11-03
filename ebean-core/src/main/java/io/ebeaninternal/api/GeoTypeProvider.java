package io.ebeaninternal.api;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.server.type.GeoTypeBinder;

/**
 * Provider of Geometry type binder support.
 */
public interface GeoTypeProvider {

  /**
   * Create a binder for binding geometry types.
   */
  GeoTypeBinder createBinder(DatabaseBuilder.Settings config);
}
