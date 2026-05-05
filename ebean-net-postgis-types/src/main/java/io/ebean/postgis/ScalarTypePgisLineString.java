package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import net.postgis.jdbc.geometry.LineString;

import java.sql.SQLException;

public class ScalarTypePgisLineString extends ScalarTypePgisBase<LineString> {

  public ScalarTypePgisLineString() {
    super(ExtraDbTypes.LINESTRING, LineString.class);
  }

  @Override
  public LineString parse(String value) {
    try {
      return new LineString(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
