package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.MultiLineString;

public class ScalarTypePgisMultiLineString extends ScalarTypePgisBase<MultiLineString> {

  public ScalarTypePgisMultiLineString() {
    super(ExtraDbTypes.MULTILINESTRING, MultiLineString.class);
  }

}
