package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.timezone.DataTimeZone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class RsetDataReader implements DataReader {

  private static final int bufferSize = 512;
  static final int clobBufferSize = 512;
  static final int stringInitialSize = 512;

  private final boolean unmodifiable;
  private final DataTimeZone dataTimeZone;
  private final ResultSet rset;
  protected int pos;
  private String json;

  public RsetDataReader(boolean unmodifiable, DataTimeZone dataTimeZone, ResultSet rset) {
    this.unmodifiable = unmodifiable;
    this.dataTimeZone = dataTimeZone;
    this.rset = rset;
  }

  @Override
  public boolean unmodifiable() {
    return unmodifiable;
  }

  @Override
  public final void pushJson(String json) {
    this.json = json;
  }

  @Override
  public final String popJson() {
    return json;
  }

  @Override
  public final void close() throws SQLException {
    rset.close();
  }

  @Override
  public final boolean next() throws SQLException {
    pos = 0;
    return rset.next();
  }

  @Override
  public final void incrementPos(int increment) {
    pos += increment;
  }

  protected int pos() {
    return ++pos;
  }

  @Override
  public final Array getArray() throws SQLException {
    return rset.getArray(pos());
  }

  @Override
  public final Object getObject() throws SQLException {
    return rset.getObject(pos());
  }

  @Override
  public final <T> T getObject(Class<T> cls) throws SQLException {
    return rset.getObject(pos(), cls);
  }

  @Override
  public final BigDecimal getBigDecimal() throws SQLException {
    return rset.getBigDecimal(pos());
  }

  @Override
  public final InputStream getBinaryStream() throws SQLException {
    return rset.getBinaryStream(pos());
  }

  @Override
  public final Boolean getBoolean() throws SQLException {
    boolean v = rset.getBoolean(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public final Byte getByte() throws SQLException {
    byte v = rset.getByte(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public final byte[] getBytes() throws SQLException {
    return rset.getBytes(pos());
  }

  @Override
  public final Date getDate() throws SQLException {
    Calendar cal = dataTimeZone.getTimeComponentTimeZone();
    if (cal != null) {
      return rset.getDate(pos(), cal);
    } else {
      return rset.getDate(pos());
    }
  }

  @Override
  public final Double getDouble() throws SQLException {
    double v = rset.getDouble(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public final Float getFloat() throws SQLException {
    float v = rset.getFloat(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public final Integer getInt() throws SQLException {
    int v = rset.getInt(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public final Long getLong() throws SQLException {
    long v = rset.getLong(pos());
    return rset.wasNull() ? null : v;
  }

  public final Ref getRef() throws SQLException {
    return rset.getRef(pos());
  }

  @Override
  public final Short getShort() throws SQLException {
    short s = rset.getShort(pos());
    return rset.wasNull() ? null : s;
  }

  @Override
  public final String getString() throws SQLException {
    return rset.getString(pos());
  }

  @Override
  public final Time getTime() throws SQLException {
    Calendar cal = dataTimeZone.getTimeComponentTimeZone();
    if (cal != null) {
      return rset.getTime(pos(), cal);
    } else {
      return rset.getTime(pos());
    }
  }

  @Override
  public final Timestamp getTimestamp() throws SQLException {
    Calendar cal = dataTimeZone.getTimeZone();
    if (cal != null) {
      return rset.getTimestamp(pos(), cal);
    } else {
      return rset.getTimestamp(pos());
    }
  }

  @Override
  public final String getStringFromStream() throws SQLException {
    Reader reader = rset.getCharacterStream(pos());
    return reader == null ? null : readStringLob(reader);
  }

  private String readStringLob(Reader reader) throws SQLException {
    char[] buffer = new char[clobBufferSize];
    int readLength;
    StringBuilder out = new StringBuilder(stringInitialSize);
    try {
      while ((readLength = reader.read(buffer)) != -1) {
        out.append(buffer, 0, readLength);
      }
      reader.close();
    } catch (IOException e) {
      throw new SQLException("IOException reading Clob " + e.getMessage());
    }
    return out.toString();
  }

  @Override
  public final byte[] getBinaryBytes() throws SQLException {
    InputStream in = rset.getBinaryStream(pos());
    return getBinaryLob(in);
  }

  private byte[] getBinaryLob(InputStream in) throws SQLException {
    if (in == null) {
      return null;
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buf = new byte[bufferSize];
      int len;
      while ((len = in.read(buf, 0, buf.length)) != -1) {
        out.write(buf, 0, len);
      }
      byte[] data = out.toByteArray();
      if (data.length == 0) {
        data = null;
      }
      in.close();
      return data;

    } catch (IOException e) {
      throw new SQLException(e.getClass().getName() + ":" + e.getMessage());
    }
  }

}
