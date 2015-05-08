package com.avaje.ebeaninternal.server.type;

import java.time.OffsetTime;
import java.time.ZoneId;

/**
 * ScalarType for java.time.OffsetTime stored as DB VARCHAR
 */
public class ScalarTypeZoneId extends ScalarTypeBaseVarchar<ZoneId> {

  public ScalarTypeZoneId() {
    super(ZoneId.class);
  }

  @Override
  public int getLength() {
    return 60;
  }

  @Override
  public String formatValue(ZoneId v) {
    return v.toString();
  }

  @Override
  public ZoneId parse(String value) {
    return ZoneId.of(value);
  }

  @Override
  public ZoneId convertFromDbString(String dbValue) {
    return ZoneId.of(dbValue);
  }

  @Override
  public String convertToDbString(ZoneId beanValue) {
    return beanValue.toString();
  }

}
