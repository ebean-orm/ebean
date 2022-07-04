package io.ebeaninternal.server.type;

import java.util.TimeZone;

/**
 * ScalarType for java.util.TimeZone which converts to and from a VARCHAR database column.
 */
final class ScalarTypeTimeZone extends ScalarTypeBaseVarchar<TimeZone> {

  ScalarTypeTimeZone() {
    super(TimeZone.class);
  }

  @Override
  public int getLength() {
    return 32;
  }

  @Override
  public TimeZone convertFromDbString(String dbValue) {
    return TimeZone.getTimeZone(dbValue);
  }

  @Override
  public String convertToDbString(TimeZone beanValue) {
    return beanValue.getID();
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
