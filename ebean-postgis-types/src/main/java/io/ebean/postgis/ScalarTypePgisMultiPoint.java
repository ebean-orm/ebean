package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import net.postgis.jdbc.geometry.MultiPoint;

import java.sql.SQLException;

public class ScalarTypePgisMultiPoint extends ScalarTypePgisBase<MultiPoint> {

  public ScalarTypePgisMultiPoint() {
    super(ExtraDbTypes.MULTIPOINT, MultiPoint.class);
  }

  @Override
  public MultiPoint parse(String value) {
    try {
      return new MultiPoint(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
