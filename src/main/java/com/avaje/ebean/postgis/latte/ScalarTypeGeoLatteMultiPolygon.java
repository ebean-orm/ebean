package com.avaje.ebean.postgis.latte;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.MultiPolygon;

public class ScalarTypeGeoLatteMultiPolygon extends ScalarTypeGeoLatteBase<MultiPolygon> {

  public ScalarTypeGeoLatteMultiPolygon() {
    super(ExtraDbTypes.MULTIPOLYGON, MultiPolygon.class);
  }

}
