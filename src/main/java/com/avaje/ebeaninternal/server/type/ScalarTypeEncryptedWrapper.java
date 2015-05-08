package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class ScalarTypeEncryptedWrapper<T> implements ScalarType<T> {

  private final ScalarType<T> wrapped;

  private final DataEncryptSupport dataEncryptSupport;

  private final ScalarTypeBytesBase byteArrayType;

  public ScalarTypeEncryptedWrapper(ScalarType<T> wrapped, ScalarTypeBytesBase byteArrayType, DataEncryptSupport dataEncryptSupport) {
    this.wrapped = wrapped;
    this.byteArrayType = byteArrayType;
    this.dataEncryptSupport = dataEncryptSupport;
  }

  @Override
  public boolean isMutable() {
    return wrapped.isMutable();
  }

  @Override
  public boolean isDirty(Object value) {
    return wrapped.isDirty(value);
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    return wrapped.readData(dataInput);
  }

  @Override
  public void writeData(DataOutput dataOutput, T v) throws IOException {
    wrapped.writeData(dataOutput, v);
  }

  @Override
  public T read(DataReader dataReader) throws SQLException {

    byte[] data = dataReader.getBytes();
    String formattedValue = dataEncryptSupport.decryptObject(data);
    if (formattedValue == null) {
      return null;
    }
    return wrapped.parse(formattedValue);
  }

  private byte[] encrypt(T value) {
    String formatValue = wrapped.formatValue(value);
    return dataEncryptSupport.encryptObject(formatValue);
  }

  @Override
  public void bind(DataBind b, T value) throws SQLException {

    byte[] encryptedValue = encrypt(value);
    byteArrayType.bind(b, encryptedValue);
  }

  @Override
  public int getJdbcType() {
    return byteArrayType.getJdbcType();
  }

  @Override
  public int getLength() {
    return byteArrayType.getLength();
  }

  @Override
  public Class<T> getType() {
    return wrapped.getType();
  }

  @Override
  public boolean isDateTimeCapable() {
    return wrapped.isDateTimeCapable();
  }

  @Override
  public boolean isJdbcNative() {
    return false;
  }

  @Override
  public void loadIgnore(DataReader dataReader) {
    wrapped.loadIgnore(dataReader);
  }

  @Override
  @SuppressWarnings("unchecked")
  public String format(Object v) {
    return formatValue((T) v);
  }

  @Override
  public String formatValue(T v) {
    return wrapped.formatValue(v);
  }

  @Override
  public T parse(String value) {
    return wrapped.parse(value);
  }

  @Override
  public T convertFromMillis(long systemTimeMillis) {
    return wrapped.convertFromMillis(systemTimeMillis);
  }

  @Override
  public T toBeanType(Object value) {
    return wrapped.toBeanType(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return wrapped.toJdbcType(value);
  }

  @Override
  public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
    wrapped.accumulateScalarTypes(propName, list);
  }

  @Override
  public T jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return wrapped.jsonRead(ctx, event);
  }
  
  @Override
  public void jsonWrite(JsonGenerator ctx, String name, T value) throws IOException {
    wrapped.jsonWrite(ctx, name, value);
  }
}
