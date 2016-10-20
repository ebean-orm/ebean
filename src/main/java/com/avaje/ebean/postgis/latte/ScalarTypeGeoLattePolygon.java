package com.avaje.ebean.postgis.latte;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.Polygon;

public class ScalarTypeGeoLattePolygon extends ScalarTypeGeoLatteBase<Polygon> {

  public ScalarTypeGeoLattePolygon() {
    super(ExtraDbTypes.POLYGON, Polygon.class);
  }
}
