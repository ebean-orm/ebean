package com.avaje.ebean.postgis.latte;

import com.avaje.ebeaninternal.server.type.CtCompoundTypeScalarList;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.postgis.PGgeometry;
import org.postgis.PGgeometryLW;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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
  public void bind(DataBind bind, T value) throws SQLException {

    if (value == null) {
      bind.setNull(Types.OTHER);
    } else {
      String wkt = Wkt.newEncoder(Wkt.Dialect.POSTGIS_EWKT_1).encode(value);
      bind.setObject(new PGgeometryLW(wkt));
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
  public T readData(DataInput dataInput) throws IOException {
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, T v) throws IOException {

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
  public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {

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
  public T jsonRead(JsonParser parser) throws IOException {
    return null;
  }

  @Override
  public void jsonWrite(JsonGenerator writer, T value) throws IOException {

  }
}
