package com.avaje.ebeaninternal.server.type;

import java.util.TimeZone;

/**
 * ScalarType for java.util.TimeZone which converts to and from a VARCHAR database column.
 */
public class ScalarTypeTimeZone extends ScalarTypeBaseVarchar<TimeZone> {

  public ScalarTypeTimeZone() {
    super(TimeZone.class);
  }

  @Override
  public int getLength() {
    return 20;
  }

  @Override
  public TimeZone convertFromDbString(String dbValue) {
    return TimeZone.getTimeZone(dbValue);
  }

  @Override
  public String convertToDbString(TimeZone beanValue) {
    return ((TimeZone) beanValue).getID();
  }

  @Override
  public String formatValue(TimeZone v) {
    return v.getID();
  }

  @Override
  public TimeZone parse(String value) {
    return TimeZone.getTimeZone(value);
  }

}
