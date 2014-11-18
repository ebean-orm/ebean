package com.avaje.ebeaninternal.server.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class ScalarTypeUUIDBinary extends ScalarTypeBase<UUID> {

  protected ScalarTypeUUIDBinary() {
    super(UUID.class, false, Types.BINARY);
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
    return convertFromBytes((byte[]) value);
  }

  @Override
  public String formatValue(UUID v) {
    return v.toString();
  }

  @Override
  public UUID parse(String value) {
    return UUID.fromString(value);
  }

  @Override
  public UUID convertFromMillis(long dateTime) {
    throw new IllegalStateException("Never called");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
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

  @Override
  public UUID readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return parse(dataInput.readUTF());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, UUID value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeUTF(format(value));
    }
  }

  @Override
  public UUID jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return UUID.fromString(ctx.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, UUID value) throws IOException {
    ctx.writeStringField(name, value.toString());
  }

}
