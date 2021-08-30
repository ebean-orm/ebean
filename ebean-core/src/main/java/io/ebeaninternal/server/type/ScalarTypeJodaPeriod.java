package io.ebeaninternal.server.type;

import org.joda.time.Period;

/**
 * ScalarType for Joda Period stored as DB VARCHAR
 */
final class ScalarTypeJodaPeriod extends ScalarTypeBaseVarchar<Period> {

  ScalarTypeJodaPeriod() {
    super(Period.class);
  }

  @Override
  public int getLength() {
    return 50;
  }

  @Override
  public String formatValue(Period v) {
    return v.toString();
  }

  @Override
  public Period parse(String value) {
    return Period.parse(value);
  }

  @Override
  public Period convertFromDbString(String dbValue) {
    return Period.parse(dbValue);
  }

  @Override
  public String convertToDbString(Period beanValue) {
    return beanValue.toString();
  }

}
