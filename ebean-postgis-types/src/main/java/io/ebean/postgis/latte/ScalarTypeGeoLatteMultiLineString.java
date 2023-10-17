package io.ebean.postgis.latte;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.MultiLineString;

public class ScalarTypeGeoLatteMultiLineString extends ScalarTypeGeoLatteBase<MultiLineString> {

  public ScalarTypeGeoLatteMultiLineString() {
    super(ExtraDbTypes.MULTILINESTRING, MultiLineString.class);
  }

}
