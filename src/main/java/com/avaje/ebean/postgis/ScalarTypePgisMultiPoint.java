package com.avaje.ebean.postgis;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;

public class ScalarTypePgisMultiPoint extends ScalarTypePgisBase<MultiPoint> {

  public ScalarTypePgisMultiPoint() {
    super(ExtraDbTypes.MULTIPOINT, MultiPoint.class);
  }

}
