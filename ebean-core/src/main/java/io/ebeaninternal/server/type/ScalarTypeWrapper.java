package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.ScalarTypeConverter;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A ScalarType that uses a ScalarTypeConverter to convert to and from another underlying
 * ScalarType.
 * <p>
 * Enables the use of a simple interface to add additional scalarTypes.
 * </p>
 *
 * @param <B> the logical type
 * @param <S> the underlying scalar type this is converted to
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

  @Override
  public String toString() {
    return "ScalarTypeWrapper " + wrapperType + " to " + scalarType.type();
  }

  @Override
  public long asVersion(B value) {
    return scalarType.asVersion(converter.unwrapValue(value));
  }

  @Override
  public boolean binary() {
    return scalarType.binary();
  }

  @Override
  public boolean mutable() {
    return scalarType.mutable();
  }

  @Override
  public boolean isDirty(Object value) {
    return scalarType.isDirty(value);
  }

  @Override
  public B readData(DataInput dataInput) throws IOException {
    S unwrapValue = scalarType.readData(dataInput);
    return converter.wrapValue(unwrapValue);
  }

  @Override
  public void writeData(DataOutput dataOutput, B value) throws IOException {
    S unwrapValue = converter.unwrapValue(value);
    scalarType.writeData(dataOutput, unwrapValue);
  }

  @Override
  public void bind(DataBinder binder, B value) throws SQLException {
    if (value == null) {
      scalarType.bind(binder, null);
    } else {
      S sv = converter.unwrapValue(value);
      scalarType.bind(binder, sv);
    }
  }

  @Override
  public int jdbcType() {
    return scalarType.jdbcType();
  }

  @Override
  public int length() {
    return scalarType.length();
  }

  @Override
  public Class<B> type() {
    return wrapperType;
  }

  @Override
  public boolean jdbcNative() {
    return false;
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
  public B read(DataReader reader) throws SQLException {
    S sv = scalarType.read(reader);
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
    if (type().isAssignableFrom(value.getClass())) {
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
    return scalarType.toJdbcType(sv);
  }

  public ScalarType<?> getScalarType() {
    return this;
  }

  @Override
  public B jsonRead(JsonParser parser) throws IOException {
    S object = scalarType.jsonRead(parser);
    return converter.wrapValue(object);
  }

  @Override
  public void jsonWrite(JsonGenerator writer, B beanValue) throws IOException {
    S unwrapValue = converter.unwrapValue(beanValue);
    scalarType.jsonWrite(writer, unwrapValue);
  }

  @Override
  public DocPropertyType docType() {
    return scalarType.docType();
  }

}
