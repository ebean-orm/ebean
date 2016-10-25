package com.avaje.ebean.postgis.latte;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.MultiPoint;

public class ScalarTypeGeoLatteMultiPoint extends ScalarTypeGeoLatteBase<MultiPoint> {

  public ScalarTypeGeoLatteMultiPoint() {
    super(ExtraDbTypes.MULTIPOINT, MultiPoint.class);
  }
}
