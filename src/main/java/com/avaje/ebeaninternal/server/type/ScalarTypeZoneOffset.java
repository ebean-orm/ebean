package com.avaje.ebeaninternal.server.type;

import java.time.ZoneOffset;

/**
 * ScalarType for java.time.OffsetTime stored as DB VARCHAR
 */
public class ScalarTypeZoneOffset extends ScalarTypeBaseVarchar<ZoneOffset> {

  public ScalarTypeZoneOffset() {
    super(ZoneOffset.class);
  }

  @Override
  public int getLength() {
    return 60;
  }

  @Override
  public String formatValue(ZoneOffset v) {
    return v.toString();
  }

  @Override
  public ZoneOffset parse(String value) {
    return ZoneOffset.of(value);
  }

  @Override
  public ZoneOffset convertFromDbString(String dbValue) {
    return ZoneOffset.of(dbValue);
  }

  @Override
  public String convertToDbString(ZoneOffset beanValue) {
    return beanValue.toString();
  }

}
