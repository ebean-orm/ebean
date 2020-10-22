package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.MultiPolygon;

public class ScalarTypePgisMultiPolygon extends ScalarTypePgisBase<MultiPolygon> {

  public ScalarTypePgisMultiPolygon() {
    super(ExtraDbTypes.MULTIPOLYGON, MultiPolygon.class);
  }

}
