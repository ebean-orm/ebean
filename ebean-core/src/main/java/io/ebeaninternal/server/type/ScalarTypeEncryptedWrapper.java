package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

public final class ScalarTypeEncryptedWrapper<T> implements ScalarType<T>, LocalEncryptedType {

  private final ScalarType<T> wrapped;
  private final DataEncryptSupport dataEncryptSupport;
  private final ScalarTypeBytesBase byteArrayType;

  public ScalarTypeEncryptedWrapper(ScalarType<T> wrapped, ScalarTypeBytesBase byteArrayType, DataEncryptSupport dataEncryptSupport) {
    this.wrapped = wrapped;
    this.byteArrayType = byteArrayType;
    this.dataEncryptSupport = dataEncryptSupport;
  }

  @Override
  public Object localEncrypt(Object value) {
    String formatValue = wrapped.format(value);
    return dataEncryptSupport.encryptObject(formatValue);
  }

  @Override
  public boolean binary() {
    return wrapped.binary();
  }

  @Override
  public boolean mutable() {
    return wrapped.mutable();
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
  public T read(DataReader reader) throws SQLException {
    byte[] data = reader.getBytes();
    String formattedValue = dataEncryptSupport.decryptObject(data);
    if (formattedValue == null) {
      return null;
    }
    return wrapped.parse(formattedValue);
  }

  private byte[] encrypt(T value) {
    if (value == null) {
      return null;
    }
    String formatValue = wrapped.formatValue(value);
    return dataEncryptSupport.encryptObject(formatValue);
  }

  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    byte[] encryptedValue = encrypt(value);
    byteArrayType.bind(binder, encryptedValue);
  }

  @Override
  public int jdbcType() {
    return byteArrayType.jdbcType();
  }

  @Override
  public int length() {
    return byteArrayType.length();
  }

  @Override
  public Class<T> type() {
    return wrapped.type();
  }

  @Override
  public boolean jdbcNative() {
    return false;
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
  public T jsonRead(JsonParser parser) throws IOException {
    return wrapped.jsonRead(parser);
  }

  @Override
  public void jsonWrite(JsonGenerator writer, T value) throws IOException {
    wrapped.jsonWrite(writer, value);
  }

  @Override
  public DocPropertyType docType() {
    return wrapped.docType();
  }

}
