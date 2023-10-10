package io.ebean.postgis;

import io.ebean.config.dbplatform.ExtraDbTypes;
import org.postgis.MultiLineString;

import java.sql.SQLException;

public class ScalarTypePgisMultiLineString extends ScalarTypePgisBase<MultiLineString> {

  public ScalarTypePgisMultiLineString() {
    super(ExtraDbTypes.MULTILINESTRING, MultiLineString.class);
  }

  @Override
  public MultiLineString parse(String value) {
    try {
      return new MultiLineString(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
