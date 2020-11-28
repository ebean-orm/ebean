package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;

import java.sql.SQLException;

/**
 * Binder for Geometry types.
 */
public interface GeoTypeBinder {

  /**
   * Bind the geometry type.
   */
  void bind(DataBinder binder, int dataType, Object data) throws SQLException;
}
