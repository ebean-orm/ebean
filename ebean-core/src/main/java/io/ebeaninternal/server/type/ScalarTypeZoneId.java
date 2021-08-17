package io.ebeaninternal.server.type;

import java.time.ZoneId;

/**
 * ScalarType for java.time.OffsetTime stored as DB VARCHAR
 */
final class ScalarTypeZoneId extends ScalarTypeBaseVarchar<ZoneId> {

  ScalarTypeZoneId() {
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
