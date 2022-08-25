package io.ebean.postgis;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;
import org.postgis.Geometry;
import org.postgis.GeometryBuilder;
import org.postgis.PGgeometry;
import org.postgis.PGgeometryLW;
import org.postgis.binary.BinaryParser;
import org.postgresql.util.PGobject;

import java.io.DataInput;
import java.io.DataOutput;
import java.sql.SQLException;
import java.sql.Types;

abstract class ScalarTypePgisBase<T extends Geometry> implements ScalarType<T> {

  private final int jdbcType;

  private final Class<T> cls;

  ScalarTypePgisBase(int jdbcType, Class<T> cls) {
    this.jdbcType = jdbcType;
    this.cls = cls;
  }

  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.NULL);
    } else {
      binder.setObject(new PGgeometryLW(value));
    }
  }


  @Override
  @SuppressWarnings("unchecked")
  public T read(DataReader reader) throws SQLException {
    Object object = reader.getObject();
    if (object == null) {
      return null;
    }
    if (object instanceof PGgeometryLW) {
      return (T) ((PGgeometryLW) object).getGeometry();

    } else if (object instanceof PGgeometry) {
      return (T) ((PGgeometry) object).getGeometry();

    } else if (object instanceof PGobject) {
      return (T) GeometryBuilder.geomFromString(((PGobject) object).getValue(), new BinaryParser(), false);

    } else {
      throw new IllegalStateException("Could not convert from " + object.getClass() + " to " + cls);
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
  public void loadIgnore(DataReader reader) {
    reader.incrementPos(1);
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
    return null;
  }

  @Override
  public String format(Object value) {
    return null;
  }

  @Override
  public T parse(String value) {
    return null;
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
