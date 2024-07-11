package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import net.postgis.jdbc.geometry.MultiPolygon;

import java.sql.SQLException;

public class ScalarTypePgisMultiPolygon extends ScalarTypePgisBase<MultiPolygon> {

  public ScalarTypePgisMultiPolygon() {
    super(ExtraDbTypes.MULTIPOLYGON, MultiPolygon.class);
  }

  @Override
  public MultiPolygon parse(String value) {
    try {
      return new MultiPolygon(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
