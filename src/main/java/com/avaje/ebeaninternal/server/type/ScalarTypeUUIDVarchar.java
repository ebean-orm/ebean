package com.avaje.ebeaninternal.server.type;

import java.io.IOException;
import java.util.UUID;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

  public UUID toBeanType(Object value) {
    return BasicTypeConverter.toUUID(value);
  }

  public Object toJdbcType(Object value) {
    return BasicTypeConverter.convert(value, jdbcType);
  }

  public String formatValue(UUID v) {
    return v.toString();
  }

  public UUID parse(String value) {
    return UUID.fromString(value);
  }

  @Override
  public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return UUID.fromString(ctx.getValueAsString());
  }

}
