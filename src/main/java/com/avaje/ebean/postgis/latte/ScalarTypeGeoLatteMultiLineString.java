package com.avaje.ebean.postgis.latte;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPolygon;

public class ScalarTypeGeoLatteMultiLineString extends ScalarTypeGeoLatteBase<MultiLineString> {

  public ScalarTypeGeoLatteMultiLineString() {
    super(ExtraDbTypes.MULTILINESTRING, MultiLineString.class);
  }

}
