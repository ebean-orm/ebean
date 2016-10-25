package com.avaje.ebean.postgis;

import com.avaje.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.LineString;
import org.postgis.MultiLineString;

public class ScalarTypePgisMultiLineString extends ScalarTypePgisBase<MultiLineString> {

  public ScalarTypePgisMultiLineString() {
    super(ExtraDbTypes.MULTILINESTRING, MultiLineString.class);
  }

}
