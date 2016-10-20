package com.avaje.ebean.postgis.latte;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.Point;

public class ScalarTypeGeoLattePoint extends ScalarTypeGeoLatteBase<Point> {

  public ScalarTypeGeoLattePoint() {
    super(ExtraDbTypes.POINT, Point.class);
  }
}
