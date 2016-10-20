package com.avaje.ebean.postgis;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.Polygon;

public class ScalarTypePgisPolygon extends ScalarTypePgisBase<Polygon> {

  public ScalarTypePgisPolygon() {
    super(ExtraDbTypes.POLYGON, Polygon.class);
  }
}
