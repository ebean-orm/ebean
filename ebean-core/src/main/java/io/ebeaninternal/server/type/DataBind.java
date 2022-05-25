package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.timezone.DataTimeZone;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataBind implements DataBinder {

  private final DataTimeZone dataTimeZone;
  private final PreparedStatement pstmt;
  private final Connection connection;
  private final StringBuilder bindLog = new StringBuilder();
  private List<InputStream> inputStreams;
  protected int pos;
  private String json;

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
  }

  @Override
  public final void setObject(Object value, int sqlType) throws SQLException {
    pstmt.setObject(++pos, value, sqlType);
  }

  @Override
  public void setNull(int jdbcType) throws SQLException {
    pstmt.setNull(++pos, jdbcType);
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
          CoreLog.log.warn("Error closing InputStream that was bound to PreparedStatement", e);
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
  public void setString(String s) throws SQLException {
    pstmt.setString(++pos, s);
  }

  @Override
  public final void setInt(int i) throws SQLException {
    pstmt.setInt(++pos, i);
  }

  @Override
  public final void setLong(long i) throws SQLException {
    pstmt.setLong(++pos, i);
  }

  @Override
  public final void setShort(short i) throws SQLException {
    pstmt.setShort(++pos, i);
  }

  @Override
  public final void setFloat(float i) throws SQLException {
    pstmt.setFloat(++pos, i);
  }

  @Override
  public final void setDouble(double i) throws SQLException {
    pstmt.setDouble(++pos, i);
  }

  @Override
  public final void setBigDecimal(BigDecimal v) throws SQLException {
    pstmt.setBigDecimal(++pos, v);
  }

  @Override
  public final void setDate(java.sql.Date v) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeComponentTimeZone();
    if (timeZone != null) {
      pstmt.setDate(++pos, v, timeZone);
    } else {
      pstmt.setDate(++pos, v);
    }
  }

  @Override
  public final void setTimestamp(Timestamp v) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeZone();
    if (timeZone != null) {
      pstmt.setTimestamp(++pos, v, timeZone);
    } else {
      pstmt.setTimestamp(++pos, v);
    }
  }

  @Override
  public final void setTime(Time v) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeComponentTimeZone();
    if (timeZone != null) {
      pstmt.setTime(++pos, v, timeZone);
    } else {
      pstmt.setTime(++pos, v);
    }
  }

  @Override
  public void setBoolean(boolean v) throws SQLException {
    pstmt.setBoolean(++pos, v);
  }

  @Override
  public void setBytes(byte[] v) throws SQLException {
    pstmt.setBytes(++pos, v);
  }

  @Override
  public void setByte(byte v) throws SQLException {
    pstmt.setByte(++pos, v);
  }

  @Override
  public void setChar(char v) throws SQLException {
    pstmt.setString(++pos, String.valueOf(v));
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
  }

  @Override
  public void setBlob(byte[] bytes) throws SQLException {
    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    pstmt.setBinaryStream(++pos, is, bytes.length);
  }

  @Override
  public void setClob(String content) throws SQLException {
    Reader reader = new StringReader(content);
    pstmt.setCharacterStream(++pos, reader, content.length());
  }

  @Override
  public void setArray(String arrayType, Object[] elements) throws SQLException {
    Array array = connection.createArrayOf(arrayType, elements);
    pstmt.setArray(++pos, array);
  }

}
