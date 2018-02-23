package io.ebeaninternal.server.type;

import java.time.Period;

/**
 * ScalarType for Joda Period stored as DB VARCHAR
 */
public class ScalarTypePeriod extends ScalarTypeBaseVarchar<Period> {

  public ScalarTypePeriod() {
    super(Period.class);
  }

  @Override
  public int getLength() {
    return 20;
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
