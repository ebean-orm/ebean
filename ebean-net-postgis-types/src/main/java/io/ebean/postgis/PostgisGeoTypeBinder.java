package io.ebean.postgis;

import io.ebean.core.type.DataBinder;
import io.ebeaninternal.server.type.GeoTypeBinder;
import net.postgis.jdbc.PGgeometryLW;
import net.postgis.jdbc.geometry.Geometry;

import java.sql.SQLException;

class PostgisGeoTypeBinder implements GeoTypeBinder {

  PostgisGeoTypeBinder() {
  }

  @Override
  public void bind(DataBinder binder, int dataType, Object data) throws SQLException {
      if (data instanceof Geometry) {
        binder.setObject(new PGgeometryLW((Geometry) data));
      }
  }
}
