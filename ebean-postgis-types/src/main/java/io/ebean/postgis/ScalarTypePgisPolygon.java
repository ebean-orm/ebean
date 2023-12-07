package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import net.postgis.jdbc.geometry.Polygon;

import java.sql.SQLException;

public class ScalarTypePgisPolygon extends ScalarTypePgisBase<Polygon> {

  public ScalarTypePgisPolygon() {
    super(ExtraDbTypes.POLYGON, Polygon.class);
  }

  @Override
  public Polygon parse(String value) {
    try {
      return new Polygon(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
