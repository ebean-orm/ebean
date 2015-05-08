package com.avaje.ebeaninternal.server.type;

import java.util.UUID;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.util.UUID which converts to and from a VARCHAR database column.
 */
public class ScalarTypeUUIDVarchar extends ScalarTypeBaseVarchar<UUID> {

  public ScalarTypeUUIDVarchar() {
    super(UUID.class);
  }

  @Override
  public int getLength() {
    return 40;
  }

  @Override
  public UUID convertFromDbString(String dbValue) {
    return UUID.fromString(dbValue);
  }

  @Override
  public String convertToDbString(UUID beanValue) {
    return formatValue(beanValue);
  }

  @Override
  public UUID toBeanType(Object value) {
    return BasicTypeConverter.toUUID(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.convert(value, jdbcType);
  }

  @Override
  public String formatValue(UUID v) {
    return v.toString();
  }

  @Override
  public UUID parse(String value) {
    return UUID.fromString(value);
  }

}
