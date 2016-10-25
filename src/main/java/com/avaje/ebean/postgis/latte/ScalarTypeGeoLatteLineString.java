package com.avaje.ebean.postgis.latte;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;

public class ScalarTypeGeoLatteLineString extends ScalarTypeGeoLatteBase<LineString> {

  public ScalarTypeGeoLatteLineString() {
    super(ExtraDbTypes.LINESTRING, LineString.class);
  }

}
