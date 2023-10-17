package io.ebean.postgis.latte;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.geolatte.geom.MultiPolygon;

public class ScalarTypeGeoLatteMultiPolygon extends ScalarTypeGeoLatteBase<MultiPolygon> {

  public ScalarTypeGeoLatteMultiPolygon() {
    super(ExtraDbTypes.MULTIPOLYGON, MultiPolygon.class);
  }

}
