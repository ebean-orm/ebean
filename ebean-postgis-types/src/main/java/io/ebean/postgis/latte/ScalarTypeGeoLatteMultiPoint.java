package io.ebean.postgis.latte;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.MultiPoint;

public class ScalarTypeGeoLatteMultiPoint extends ScalarTypeGeoLatteBase<MultiPoint> {

  public ScalarTypeGeoLatteMultiPoint() {
    super(ExtraDbTypes.MULTIPOINT, MultiPoint.class);
  }
}
