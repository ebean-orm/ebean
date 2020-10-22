package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.Polygon;

public class ScalarTypePgisPolygon extends ScalarTypePgisBase<Polygon> {

  public ScalarTypePgisPolygon() {
    super(ExtraDbTypes.POLYGON, Polygon.class);
  }
}
