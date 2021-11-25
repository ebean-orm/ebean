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

  private final DataTimeZone dataTimeZone;
  private final ResultSet rset;
  protected int pos;
  private String json;

  public RsetDataReader(DataTimeZone dataTimeZone, ResultSet rset) {
    this.dataTimeZone = dataTimeZone;
    this.rset = rset;
  }

  @Override
  public void pushJson(String json) {
    this.json = json;
  }

  @Override
  public String popJson() {
    return json;
  }

  @Override
  public void close() throws SQLException {
    rset.close();
  }

  @Override
  public boolean next() throws SQLException {
    pos = 0;
    return rset.next();
  }

  @Override
  public void incrementPos(int increment) {
    pos += increment;
  }

  protected int pos() {
    return ++pos;
  }

  @Override
  public Array getArray() throws SQLException {
    return rset.getArray(pos());
  }

  @Override
  public Object getObject() throws SQLException {
    return rset.getObject(pos());
  }

  @Override
  public BigDecimal getBigDecimal() throws SQLException {
    return rset.getBigDecimal(pos());
  }

  @Override
  public InputStream getBinaryStream() throws SQLException {
    return rset.getBinaryStream(pos());
  }

  @Override
  public Boolean getBoolean() throws SQLException {
    boolean v = rset.getBoolean(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public Byte getByte() throws SQLException {
    byte v = rset.getByte(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public byte[] getBytes() throws SQLException {
    return rset.getBytes(pos());
  }

  @Override
  public Date getDate() throws SQLException {
    Calendar cal = dataTimeZone.getLocalTimeZone();
    if (cal != null) {
      return rset.getDate(pos(), cal);
    } else {
      return rset.getDate(pos());
    }
  }

  @Override
  public Double getDouble() throws SQLException {
    double v = rset.getDouble(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public Float getFloat() throws SQLException {
    float v = rset.getFloat(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public Integer getInt() throws SQLException {
    int v = rset.getInt(pos());
    return rset.wasNull() ? null : v;
  }

  @Override
  public Long getLong() throws SQLException {
    long v = rset.getLong(pos());
    return rset.wasNull() ? null : v;
  }

  public Ref getRef() throws SQLException {
    return rset.getRef(pos());
  }

  @Override
  public Short getShort() throws SQLException {
    short s = rset.getShort(pos());
    return rset.wasNull() ? null : s;
  }

  @Override
  public String getString() throws SQLException {
    return rset.getString(pos());
  }

  @Override
  public Time getTime() throws SQLException {
    Calendar cal = dataTimeZone.getLocalTimeZone();
    if (cal != null) {
      return rset.getTime(pos(), cal);
    } else {
      return rset.getTime(pos());
    }
  }

  @Override
  public Timestamp getTimestamp(boolean isLocal) throws SQLException {
    Calendar cal = isLocal ? dataTimeZone.getLocalTimeZone() : dataTimeZone.getTimeZone();
    if (cal != null) {
      return rset.getTimestamp(pos(), cal);
    } else {
      return rset.getTimestamp(pos());
    }
  }

  @Override
  public String getStringFromStream() throws SQLException {
    Reader reader = rset.getCharacterStream(pos());
    return reader == null ? null : readStringLob(reader);
  }

  protected String readStringLob(Reader reader) throws SQLException {
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
  public byte[] getBinaryBytes() throws SQLException {
    InputStream in = rset.getBinaryStream(pos());
    return getBinaryLob(in);
  }

  protected byte[] getBinaryLob(InputStream in) throws SQLException {
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
