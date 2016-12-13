package io.ebean.postgis.latte;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.LineString;

public class ScalarTypeGeoLatteLineString extends ScalarTypeGeoLatteBase<LineString> {

  public ScalarTypeGeoLatteLineString() {
    super(ExtraDbTypes.LINESTRING, LineString.class);
  }

}
