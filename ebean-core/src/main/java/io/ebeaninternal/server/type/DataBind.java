package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(DataBind.class);

  private final DataTimeZone dataTimeZone;

  private final PreparedStatement pstmt;

  private final Connection connection;

  private final StringBuilder bindLog = new StringBuilder();

  private List<InputStream> inputStreams;

  protected int pos;

  public DataBind(DataTimeZone dataTimeZone, PreparedStatement pstmt, Connection connection) {
    this.dataTimeZone = dataTimeZone;
    this.pstmt = pstmt;
    this.connection = connection;
  }

  @Override
  public StringBuilder append(Object entry) {
    return bindLog.append(entry);
  }

  @Override
  public StringBuilder log() {
    return bindLog;
  }

  @Override
  public void close() throws SQLException {
    pstmt.close();
  }

  @Override
  public int currentPos() {
    return pos;
  }

  @Override
  public void setObject(Object value) throws SQLException {
    pstmt.setObject(++pos, value);
  }

  @Override
  public void setObject(Object value, int sqlType) throws SQLException {
    pstmt.setObject(++pos, value, sqlType);
  }

  @Override
  public void setNull(int jdbcType) throws SQLException {
    pstmt.setNull(++pos, jdbcType);
  }

  @Override
  public int nextPos() {
    return ++pos;
  }

  @Override
  public void decrementPos() {
    --pos;
  }

  @Override
  public int executeUpdate() throws SQLException {
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
          log.warn("Error closing InputStream that was bound to PreparedStatement", e);
        }
      }
      inputStreams = null;
    }
  }

  @Override
  public PreparedStatement getPstmt() {
    return pstmt;
  }

  @Override
  public void setString(String s) throws SQLException {
    pstmt.setString(++pos, s);
  }

  @Override
  public void setInt(int i) throws SQLException {
    pstmt.setInt(++pos, i);
  }

  @Override
  public void setLong(long i) throws SQLException {
    pstmt.setLong(++pos, i);
  }

  @Override
  public void setShort(short i) throws SQLException {
    pstmt.setShort(++pos, i);
  }

  @Override
  public void setFloat(float i) throws SQLException {
    pstmt.setFloat(++pos, i);
  }

  @Override
  public void setDouble(double i) throws SQLException {
    pstmt.setDouble(++pos, i);
  }

  @Override
  public void setBigDecimal(BigDecimal v) throws SQLException {
    pstmt.setBigDecimal(++pos, v);
  }

  @Override
  public void setDate(java.sql.Date v) throws SQLException {
    Calendar timeZone = dataTimeZone.getDateTimeZone();
    if (timeZone != null) {
      pstmt.setDate(++pos, v, timeZone);
    } else {
      pstmt.setDate(++pos, v);
    }
  }

  @Override
  public void setTimestamp(Timestamp v) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeZone();
    if (timeZone != null) {
      pstmt.setTimestamp(++pos, v, timeZone);
    } else {
      pstmt.setTimestamp(++pos, v);
    }
  }

  @Override
  public void setTime(Time v) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeZone();
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
