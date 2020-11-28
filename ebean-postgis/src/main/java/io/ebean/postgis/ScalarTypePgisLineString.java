package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.LineString;

public class ScalarTypePgisLineString extends ScalarTypePgisBase<LineString> {

  public ScalarTypePgisLineString() {
    super(ExtraDbTypes.LINESTRING, LineString.class);
  }

}
