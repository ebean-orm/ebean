package io.ebeaninternal.server.bind;

import io.ebean.config.dbplatform.InputStreamInfo;
import io.ebean.core.type.DataBinder;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.timezone.DataTimeZone;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.System.Logger.Level.WARNING;

public class DataBind implements DataBinder {

  private static final Object UNBOUND = new Object();
  private final DataTimeZone dataTimeZone;
  private final PreparedStatement pstmt;
  private final Connection connection;
  private final StringBuilder bindLog = new StringBuilder();
  private List<InputStream> inputStreams;
  protected int pos;
  private String json;

  private Object lastObject = UNBOUND;

  public DataBind(DataTimeZone dataTimeZone, PreparedStatement pstmt, Connection connection) {
    this.dataTimeZone = dataTimeZone;
    this.pstmt = pstmt;
    this.connection = connection;
  }

  @Override
  public final void pushJson(String json) {
    assert this.json == null; // we can only push one value
    this.json = json;
  }

  @Override
  public final String popJson() {
    String ret = json;
    json = null;
    return ret;
  }

  @Override
  public final StringBuilder append(Object entry) {
    return bindLog.append(entry);
  }

  @Override
  public final StringBuilder log() {
    return bindLog;
  }

  @Override
  public final void close() throws SQLException {
    pstmt.close();
  }

  @Override
  public final int currentPos() {
    return pos;
  }

  @Override
  public void setObject(Object value) throws SQLException {
    pstmt.setObject(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setObject(Object value, int sqlType) throws SQLException {
    pstmt.setObject(++pos, value, sqlType);
    lastObject = value;
  }

  @Override
  public void setNull(int jdbcType) throws SQLException {
    pstmt.setNull(++pos, jdbcType);
    lastObject = null;
  }

  @Override
  public final int nextPos() {
    return ++pos;
  }

  @Override
  public final void decrementPos() {
    --pos;
  }

  @Override
  public final int executeUpdate() throws SQLException {
    try {
      return pstmt.executeUpdate();
    } finally {
      closeInputStreams();
    }
  }

  private void closeInputStreams() {
    if (inputStreams != null) {
      for (InputStream inputStream : inputStreams) {
        try {
          inputStream.close();
        } catch (IOException e) {
          CoreLog.log.log(WARNING, "Error closing InputStream that was bound to PreparedStatement", e);
        }
      }
      inputStreams = null;
    }
  }

  @Override
  public final PreparedStatement getPstmt() {
    return pstmt;
  }

  @Override
  public void setString(String value) throws SQLException {
    pstmt.setString(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setInt(int value) throws SQLException {
    pstmt.setInt(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setLong(long value) throws SQLException {
    pstmt.setLong(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setShort(short value) throws SQLException {
    pstmt.setShort(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setFloat(float value) throws SQLException {
    pstmt.setFloat(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setDouble(double value) throws SQLException {
    pstmt.setDouble(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setBigDecimal(BigDecimal value) throws SQLException {
    pstmt.setBigDecimal(++pos, value);
    lastObject = value;
  }

  @Override
  public final void setDate(java.sql.Date value) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeComponentTimeZone();
    if (timeZone != null) {
      pstmt.setDate(++pos, value, timeZone);
    } else {
      pstmt.setDate(++pos, value);
    }
    lastObject = value;
  }

  @Override
  public final void setTimestamp(Timestamp value) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeZone();
    if (timeZone != null) {
      pstmt.setTimestamp(++pos, value, timeZone);
    } else {
      pstmt.setTimestamp(++pos, value);
    }
    lastObject = value;
  }

  @Override
  public final void setTime(Time value) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeComponentTimeZone();
    if (timeZone != null) {
      pstmt.setTime(++pos, value, timeZone);
    } else {
      pstmt.setTime(++pos, value);
    }
    lastObject = value;
  }

  @Override
  public void setBoolean(boolean value) throws SQLException {
    pstmt.setBoolean(++pos, value);
    lastObject = value;
  }

  @Override
  public void setBytes(byte[] value) throws SQLException {
    pstmt.setBytes(++pos, value);
    lastObject = value;
  }

  @Override
  public void setByte(byte value) throws SQLException {
    pstmt.setByte(++pos, value);
    lastObject = value;
  }

  @Override
  public void setChar(char value) throws SQLException {
    pstmt.setString(++pos, String.valueOf(value));
    lastObject = value;
  }

  @Override
  public List<InputStream> getInputStreams() {
    return inputStreams;
  }

  @Override
  public void setBinaryStream(InputStream inputStream, long length) throws SQLException {
    if (inputStreams == null) {
      inputStreams = new ArrayList<>();
    }
    inputStreams.add(inputStream);
    pstmt.setBinaryStream(++pos, inputStream, length);
    lastObject = new InputStreamInfo(inputStream, length);
  }

  @Override
  public void setBlob(byte[] bytes) throws SQLException {
    pstmt.setBinaryStream(++pos, new ByteArrayInputStream(bytes), bytes.length);
    lastObject = bytes;
  }

  @Override
  public void setClob(String content) throws SQLException {
    pstmt.setCharacterStream(++pos, new StringReader(content), content.length());
    lastObject = content;
  }

  @Override
  public void setArray(String arrayType, Object[] elements) throws SQLException {
    pstmt.setArray(++pos, connection.createArrayOf(arrayType, elements));
    lastObject = elements;
  }

  @Override
  public Object popLastObject() {
    Object ret = lastObject;
    lastObject = UNBOUND;
    if (ret == UNBOUND) {
      throw new IllegalStateException("No object bound");
    }
    return ret;
  }
}
