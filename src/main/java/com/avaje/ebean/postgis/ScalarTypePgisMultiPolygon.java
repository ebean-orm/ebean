package com.avaje.ebean.postgis;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.MultiPolygon;

public class ScalarTypePgisMultiPolygon extends ScalarTypePgisBase<MultiPolygon> {

  public ScalarTypePgisMultiPolygon() {
    super(ExtraDbTypes.POLYGON, MultiPolygon.class);
  }

}
