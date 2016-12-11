package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class ScalarTypeUUIDBinary extends ScalarTypeUUIDBase {

  protected ScalarTypeUUIDBinary() {
    super(false, Types.BINARY);
  }

  @Override
  public int getLength() {
    return 16;
  }

  @Override
  public Object toJdbcType(Object value) {
    return convertToBytes(value);
  }

  @Override
  public UUID toBeanType(Object value) {
    if (value instanceof byte[]) {
      return convertFromBytes((byte[]) value);
    } else {
      return BasicTypeConverter.toUUID(value);
    }
  }

  @Override
  public void bind(DataBind b, UUID value) throws SQLException {
    if (value == null) {
      b.setNull(Types.BINARY);
    } else {
      b.setBytes(convertToBytes(value));
    }
  }

  @Override
  public UUID read(DataReader dataReader) throws SQLException {
    byte[] bytes = dataReader.getBytes();
    if (bytes == null) {
      return null;
    } else {
      return convertFromBytes(bytes);
    }
  }

  /**
   * Convert from byte[] to UUID.
   */
  public static UUID convertFromBytes(byte[] bytes) {

    int usableBytes = Math.min(bytes.length, 16);

    // Need exactly 16 bytes - pad the input if not enough bytes are provided
    // Use provided bytes in the least significant position; if more than 16 bytes are given,
    // then use the first 16 bytes from the array;
    byte[] barr = new byte[16];
    for (int i = 15, j = usableBytes - 1; j >= 0; i--, j--) {
      barr[i] = bytes[j];
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(barr);
    DataInputStream inputStream = new DataInputStream(bais);

    try {
      long msb = inputStream.readLong();
      long lsb = inputStream.readLong();
      return new UUID(msb, lsb);

    } catch (IOException e) {
      throw new RuntimeException("Not Expecting this", e);
    }
  }

  /**
   * Convert from UUID to byte[].
   */
  public static byte[] convertToBytes(Object value) {

    UUID uuid = (UUID) value;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
    DataOutputStream outputStream = new DataOutputStream(baos);

    try {
      outputStream.writeLong(uuid.getMostSignificantBits());
      outputStream.writeLong(uuid.getLeastSignificantBits());
    } catch (IOException e) {
      throw new RuntimeException("Not Expecting this", e);
    }

    return baos.toByteArray();
  }

}
