package io.ebean.pgvector;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;
import org.postgresql.util.PGobject;

import java.io.DataInput;
import java.io.DataOutput;
import java.sql.SQLException;
import java.sql.Types;

abstract class ScalarTypePGbase<T extends PGobject> implements ScalarType<T> {

  private final int jdbcType;
  private final Class<T> cls;

  public ScalarTypePGbase(int jdbcType, Class<T> cls) {
    this.jdbcType = jdbcType;
    this.cls = cls;
  }

  @Override
  public T read(DataReader reader) throws SQLException {
    var obj=reader.getObject();
    if(obj==null) return null;
    return cls.cast(obj);
  }

  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    if(value==null) {
      binder.setNull(Types.NULL);
    } else {
      binder.setObject(value);
    }
  }

  @Override
  public boolean jdbcNative() {
    return true;
  }

  @Override
  public int jdbcType() {
    return jdbcType;
  }

  @Override
  public Class<T> type() {
    return cls;
  }

  @Override
  public T readData(DataInput dataInput) {
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, T v) {

  }

  @Override
  public Object toJdbcType(Object value) {
    return null;
  }

  @Override
  public T toBeanType(Object value) {
    return null;
  }

  @Override
  public String formatValue(T value) {
    return value.toString();
  }

  @Override
  public DocPropertyType docType() {
    return null;
  }

  @Override
  public T jsonRead(JsonParser parser) {
    return null;
  }

  @Override
  public void jsonWrite(JsonGenerator writer, T value) {

  }
}
