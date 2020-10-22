package io.ebean.postgis;

import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.GeoTypeBinder;
import org.geolatte.geom.codec.Wkt;
import org.postgis.Geometry;
import org.postgis.PGgeometryLW;

import java.sql.SQLException;

class PostgisGeoTypeBinder implements GeoTypeBinder {

  private final boolean withGeolatte;

  PostgisGeoTypeBinder(boolean withGeolatte) {
    this.withGeolatte = withGeolatte;
  }

  @Override
  public void bind(DataBind b, int dataType, Object data) throws SQLException {
      if (data instanceof Geometry) {
        b.setObject(new PGgeometryLW((Geometry) data));
      } else if (withGeolatte) {
        b.setObject(new PGgeometryLW(toWktString(data)));
      }
  }

  private String toWktString(Object data) {
    org.geolatte.geom.Geometry<?> geoLatte = (org.geolatte.geom.Geometry<?>)data;
    return Wkt.newEncoder(Wkt.Dialect.POSTGIS_EWKT_1).encode(geoLatte);
  }
}
