package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.BasicTypeConverter;
import io.ebean.core.type.ScalarTypeUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public final class ScalarTypeUUIDBinary extends ScalarTypeUUIDBase {

  private final boolean optimized;

  ScalarTypeUUIDBinary(boolean optimized) {
    super(false, Types.BINARY);
    this.optimized = optimized;
  }

  @Override
  public int length() {
    return 16;
  }

  @Override
  public Object toJdbcType(Object value) {
    return convertToBytes((UUID)value, optimized);
  }

  @Override
  public UUID toBeanType(Object value) {
    if (value instanceof byte[]) {
      return convertFromBytes((byte[]) value, optimized);
    } else {
      return BasicTypeConverter.toUUID(value, optimized);
    }
  }

  @Override
  public void bind(DataBinder binder, UUID value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.BINARY);
    } else {
      binder.setBytes(convertToBytes(value, optimized));
    }
  }

  @Override
  public UUID read(DataReader reader) throws SQLException {
    byte[] bytes = reader.getBytes();
    if (bytes == null) {
      return null;
    } else {
      return ScalarTypeUtils.uuidFromBytes(bytes, optimized);
    }
  }

  static UUID convertFromBytes(byte[] bytes, boolean optimized) {
    return ScalarTypeUtils.uuidFromBytes(bytes, optimized);
  }

  static byte[] convertToBytes(UUID value, boolean optimized) {
    return ScalarTypeUtils.uuidToBytes(value, optimized);
  }

}
