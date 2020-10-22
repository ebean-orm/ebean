package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.MultiPoint;

public class ScalarTypePgisMultiPoint extends ScalarTypePgisBase<MultiPoint> {

  public ScalarTypePgisMultiPoint() {
    super(ExtraDbTypes.MULTIPOINT, MultiPoint.class);
  }

}
