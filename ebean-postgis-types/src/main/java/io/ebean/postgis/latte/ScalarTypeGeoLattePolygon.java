package io.ebean.postgis.latte;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.Polygon;

public class ScalarTypeGeoLattePolygon extends ScalarTypeGeoLatteBase<Polygon> {

  public ScalarTypeGeoLattePolygon() {
    super(ExtraDbTypes.POLYGON, Polygon.class);
  }
}
