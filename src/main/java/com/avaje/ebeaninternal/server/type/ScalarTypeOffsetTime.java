package com.avaje.ebeaninternal.server.type;

import java.time.OffsetTime;

/**
 * ScalarType for java.time.OffsetTime stored as DB VARCHAR
 */
public class ScalarTypeOffsetTime extends ScalarTypeBaseVarchar<OffsetTime> {

  public ScalarTypeOffsetTime() {
    super(OffsetTime.class);
  }

  @Override
  public int getLength() {
    return 25;
  }

  @Override
  public String formatValue(OffsetTime v) {
    return v.toString();
  }

  @Override
  public OffsetTime parse(String value) {
    return OffsetTime.parse(value);
  }

  @Override
  public OffsetTime convertFromDbString(String dbValue) {
    return OffsetTime.parse(dbValue);
  }

  @Override
  public String convertToDbString(OffsetTime beanValue) {
    return beanValue.toString();
  }

}
