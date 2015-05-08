package com.avaje.ebeaninternal.server.type;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Encrypted ScalarType that wraps a byte[] types.
 * 
 * @author rbygrave
 * 
 */
public class ScalarTypeBytesEncrypted implements ScalarType<byte[]> {

  private final ScalarTypeBytesBase baseType;

  private final DataEncryptSupport dataEncryptSupport;

  public ScalarTypeBytesEncrypted(ScalarTypeBytesBase baseType, DataEncryptSupport dataEncryptSupport) {
    this.baseType = baseType;
    this.dataEncryptSupport = dataEncryptSupport;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public boolean isDirty(Object value) {
    return false;
  }

  public void bind(DataBind b, byte[] value) throws SQLException {
    value = dataEncryptSupport.encrypt(value);
    baseType.bind(b, value);
  }

  public int getJdbcType() {
    return baseType.getJdbcType();
  }

  public int getLength() {
    return baseType.getLength();
  }

  public Class<byte[]> getType() {
    return byte[].class;
  }

  public boolean isDateTimeCapable() {
    return baseType.isDateTimeCapable();
  }

  public boolean isJdbcNative() {
    return baseType.isJdbcNative();
  }

  public void loadIgnore(DataReader dataReader) {
    baseType.loadIgnore(dataReader);
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, byte[] value) throws IOException {
    ctx.writeBinaryField(name, (byte[]) value);
  }

  @Override
  public byte[] jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(500);
    ctx.readBinaryValue(out);
    return out.toByteArray();
  }

  public String format(Object v) {
    throw new RuntimeException("Not used");
  }

  public String formatValue(byte[] v) {
    throw new RuntimeException("Not used");
  }

  public byte[] parse(String value) {
    return baseType.parse(value);
  }

  public byte[] convertFromMillis(long systemTimeMillis) {
    return baseType.convertFromMillis(systemTimeMillis);
  }

  public byte[] read(DataReader dataReader) throws SQLException {

    byte[] data = baseType.read(dataReader);
    data = dataEncryptSupport.decrypt(data);
    return data;
  }

  public byte[] toBeanType(Object value) {
    return baseType.toBeanType(value);
  }

  public Object toJdbcType(Object value) {
    return baseType.toJdbcType(value);
  }

  public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
    baseType.accumulateScalarTypes(propName, list);
  }

  public byte[] readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      int len = dataInput.readInt();
      byte[] value = new byte[len];
      dataInput.readFully(value);
      return value;
    }
  }

  public void writeData(DataOutput dataOutput, byte[] value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeInt(value.length);
      dataOutput.write(value);
    }
  }

}
