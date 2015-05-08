package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

import com.avaje.ebean.config.ScalarTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * A ScalarType that uses a ScalarTypeConverter to convert to and from another underlying
 * ScalarType.
 * <p>
 * Enables the use of a simple interface to add additional scalarTypes.
 * </p>
 * 
 * @author rbygrave
 * 
 * @param <B>
 *          the logical type
 * @param <S>
 *          the underlying scalar type this is converted to
 */
public class ScalarTypeWrapper<B, S> implements ScalarType<B> {

  private final ScalarType<S> scalarType;

  private final ScalarTypeConverter<B, S> converter;

  private final Class<B> wrapperType;

  private final B nullValue;

  public ScalarTypeWrapper(Class<B> wrapperType, ScalarType<S> scalarType, ScalarTypeConverter<B, S> converter) {
    this.scalarType = scalarType;
    this.converter = converter;
    this.nullValue = converter.getNullValue();
    this.wrapperType = wrapperType;
  }

  public String toString() {
    return "ScalarTypeWrapper " + wrapperType + " to " + scalarType.getType();
  }

  @Override
  public boolean isMutable() {
    return scalarType.isMutable();
  }

  @Override
  public boolean isDirty(Object value) {
    return scalarType.isDirty(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public B readData(DataInput dataInput) throws IOException {
    S unwrapValue = scalarType.readData(dataInput);
    return converter.wrapValue(unwrapValue);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeData(DataOutput dataOutput, B value) throws IOException {
    S unwrapValue = converter.unwrapValue(value);
    scalarType.writeData(dataOutput, unwrapValue);
  }

  @Override
  public void bind(DataBind b, B value) throws SQLException {
    if (value == null) {
      scalarType.bind(b, null);
    } else {
      S sv = converter.unwrapValue(value);
      scalarType.bind(b, sv);
    }
  }

  @Override
  public int getJdbcType() {
    return scalarType.getJdbcType();
  }

  @Override
  public int getLength() {
    return scalarType.getLength();
  }

  @Override
  public Class<B> getType() {
    return wrapperType;
  }

  @Override
  public boolean isDateTimeCapable() {
    return scalarType.isDateTimeCapable();
  }

  @Override
  public boolean isJdbcNative() {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String format(Object v) {
    return formatValue((B) v);
  }

  @Override
  public String formatValue(B v) {
    S sv = converter.unwrapValue(v);
    return scalarType.formatValue(sv);
  }

  @Override
  public B parse(String value) {
    S sv = scalarType.parse(value);
    if (sv == null) {
      return nullValue;
    }
    return converter.wrapValue(sv);
  }

  @Override
  public B convertFromMillis(long systemTimeMillis) {
    S sv = scalarType.convertFromMillis(systemTimeMillis);
    if (sv == null) {
      return nullValue;
    }
    return converter.wrapValue(sv);
  }

  @Override
  public void loadIgnore(DataReader dataReader) {
    dataReader.incrementPos(1);
  }

  @Override
  public B read(DataReader dataReader) throws SQLException {

    S sv = scalarType.read(dataReader);
    if (sv == null) {
      return nullValue;
    }
    return converter.wrapValue(sv);
  }

  @Override
  @SuppressWarnings("unchecked")
  public B toBeanType(Object value) {
    if (value == null) {
      return nullValue;
    }
    if (getType().isAssignableFrom(value.getClass())) {
      return (B) value;
    }
    if (value instanceof String) {
      return parse((String) value);
    }
    S sv = scalarType.toBeanType(value);
    return converter.wrapValue(sv);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object toJdbcType(Object value) {

    Object sv = converter.unwrapValue((B) value);
    if (sv == null) {
      return nullValue;
    }
    return scalarType.toJdbcType(sv);
  }

  @Override
  public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
    list.addScalarType(propName, this);
  }

  public ScalarType<?> getScalarType() {
    return this;
  }

  @Override
  public B jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    S object = scalarType.jsonRead(ctx, event);
    return converter.wrapValue(object);
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, B beanValue) throws IOException {
    S unwrapValue = converter.unwrapValue(beanValue);
    scalarType.jsonWrite(ctx, name, unwrapValue);
  }

}
