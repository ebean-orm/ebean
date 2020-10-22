package io.ebeaninternal.server.type;

import java.sql.SQLException;

/**
 * Binder for Geometry types.
 */
public interface GeoTypeBinder {

  /**
   * Bind the geometry type.
   */
  void bind(DataBind b, int dataType, Object data) throws SQLException;
}
