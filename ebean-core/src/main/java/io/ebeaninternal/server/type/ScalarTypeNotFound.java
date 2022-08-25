package io.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

/**
 * Class is required as "null" key in ConcurrentHashMap.
 * @author Roland Praml, FOCONIS AG
 *
 */
class ScalarTypeNotFound implements ScalarType<Void> {

  public static final ScalarTypeNotFound INSTANCE = new ScalarTypeNotFound();
  private ScalarTypeNotFound() {  }

  @Override
  public boolean jdbcNative() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int jdbcType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<Void> type() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void read(DataReader reader) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void bind(DataBinder binder, Void value) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object toJdbcType(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void toBeanType(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String formatValue(Void value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String format(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void parse(String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DocPropertyType docType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void readData(DataInput dataInput) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeData(DataOutput dataOutput, Void v) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void jsonRead(JsonParser parser) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Void value) throws IOException {
    throw new UnsupportedOperationException();
  }

}
