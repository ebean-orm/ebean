package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.Point;

public class ScalarTypePgisPoint extends ScalarTypePgisBase<Point> {

  public ScalarTypePgisPoint() {
    super(ExtraDbTypes.POINT, Point.class);
  }

}
