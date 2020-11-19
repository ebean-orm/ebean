package io.ebean.postgis.latte;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.postgis.PGgeometry;
import org.postgis.PGgeometryLW;

import java.io.DataInput;
import java.io.DataOutput;
import java.sql.SQLException;
import java.sql.Types;

abstract class ScalarTypeGeoLatteBase<T extends Geometry> implements ScalarType<T> {

  private final int jdbcType;

  private final Class<T> cls;

  ScalarTypeGeoLatteBase(int jdbcType, Class<T> cls) {
    this.jdbcType = jdbcType;
    this.cls = cls;
  }

  @Override
  public boolean isJdbcNative() {
    return true;
  }

  @Override
  public int getJdbcType() {
    return jdbcType;
  }

  @Override
  public Class<T> getType() {
    return cls;
  }

  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.OTHER);
    } else {
      String wkt = Wkt.newEncoder(Wkt.Dialect.POSTGIS_EWKT_1).encode(value);
      binder.setObject(new PGgeometryLW(wkt));
    }
  }


  @Override
  @SuppressWarnings("unchecked")
  public T read(DataReader reader) throws SQLException {
    Object object = reader.getObject();
    if (object == null) {
      return null;
    }
    if (object instanceof PGgeometry) {
      org.postgis.Geometry geometry = ((PGgeometry) object).getGeometry();
      return (T) Wkt.newDecoder(Wkt.Dialect.POSTGIS_EWKT_1).decode(geometry.toString());
    }
    throw new IllegalStateException("Received object of type " + object.getClass().getCanonicalName());
  }

  @Override
  public T readData(DataInput dataInput) {
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, T v)  {

  }

  @Override
  public boolean isBinaryType() {
    return false;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public boolean isDirty(Object value) {
    return false;
  }

  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public void loadIgnore(DataReader reader) {

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
  public DocPropertyType getDocType() {
    return null;
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public long asVersion(T value) {
    return 0;
  }

  @Override
  public T convertFromMillis(long dateTime) {
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
