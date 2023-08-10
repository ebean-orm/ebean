package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.Point;

import java.sql.SQLException;

public class ScalarTypePgisPoint extends ScalarTypePgisBase<Point> {

  public ScalarTypePgisPoint() {
    super(ExtraDbTypes.POINT, Point.class);
  }

  @Override
  public Point parse(String value) {
    try {
      return new Point(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
