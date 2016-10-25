package com.avaje.ebean.postgis;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.LineString;
import org.postgis.Point;

public class ScalarTypePgisLineString extends ScalarTypePgisBase<LineString> {

  public ScalarTypePgisLineString() {
    super(ExtraDbTypes.LINESTRING, LineString.class);
  }

}
