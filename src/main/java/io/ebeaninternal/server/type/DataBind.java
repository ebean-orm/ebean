package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.persist.BatchedPstmt;

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

public class DataBind implements AutoCloseable {

  private final DataTimeZone dataTimeZone;

  private final PreparedStatement pstmt;

  private final Connection connection;

  private final StringBuilder bindLog = new StringBuilder();

  private List<InputStream> streams;

  private final BatchedPstmt batchedPstmt;

  private int pos;

  /**
   * Wraps a prepared statement. The statement will be closed on {@link DataBind#close()}.
   */
  public DataBind(DataTimeZone dataTimeZone, PreparedStatement pstmt, Connection connection) {
    this.dataTimeZone = dataTimeZone;
    this.pstmt = pstmt;
    this.streams = null;
    this.batchedPstmt = null;
    this.connection = connection;
  }

  /**
   * Wraps a {@link BatchedPstmt} and takes accout of batch requirements.
   */
  public DataBind(DataTimeZone dataTimeZone, BatchedPstmt batchedPstmt, Connection connection) {
    this.dataTimeZone = dataTimeZone;
    this.pstmt = batchedPstmt.getStatement();
    this.batchedPstmt = batchedPstmt;
    this.connection = connection;
  }

  /**
   * Append an entry to the bind log.
   */
  public StringBuilder append(Object entry) {
    return bindLog.append(entry);
  }

  /**
   * Return the bind log.
   */
  public StringBuilder log() {
    return bindLog;
  }

  /**
   * Close the underlying prepared statement.
   */
  @Override
  public void close() {
    // in batch mode, the BatchPstmt is responsible to close streams. Our list of streams
    // is null, so we do nothing here.
    if (streams != null) {
      for (InputStream stream : streams) {
        try {
          stream.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public int currentPos() {
    return pos;
  }

  public void setObject(Object value) throws SQLException {
    registerStream(value);
    pstmt.setObject(++pos, value);
  }

  public void setObject(Object value, int sqlType) throws SQLException {
    registerStream(value);
    pstmt.setObject(++pos, value, sqlType);
  }

  public void setNull(int jdbcType) throws SQLException {
    pstmt.setNull(++pos, jdbcType);
  }

  public int nextPos() {
    return ++pos;
  }

  public void decrementPos() {
    --pos;
  }

  public int executeUpdate() throws SQLException {
    return pstmt.executeUpdate();
  }

  public PreparedStatement getPstmt() {
    return pstmt;
  }

  public void setString(String s) throws SQLException {
    pstmt.setString(++pos, s);
  }

  public void setInt(int i) throws SQLException {
    pstmt.setInt(++pos, i);
  }

  public void setLong(long i) throws SQLException {
    pstmt.setLong(++pos, i);
  }

  public void setShort(short i) throws SQLException {
    pstmt.setShort(++pos, i);
  }

  public void setFloat(float i) throws SQLException {
    pstmt.setFloat(++pos, i);
  }

  public void setDouble(double i) throws SQLException {
    pstmt.setDouble(++pos, i);
  }

  public void setBigDecimal(BigDecimal v) throws SQLException {
    pstmt.setBigDecimal(++pos, v);
  }

  public void setDate(java.sql.Date v) throws SQLException {
    pstmt.setDate(++pos, v);
  }

  public void setTimestamp(Timestamp v) throws SQLException {
    Calendar timeZone = dataTimeZone.getTimeZone();
    if (timeZone != null) {
      pstmt.setTimestamp(++pos, v, timeZone);
    } else {
      pstmt.setTimestamp(++pos, v);
    }
  }

  public void setTime(Time v) throws SQLException {
    pstmt.setTime(++pos, v);
  }

  public void setBoolean(boolean v) throws SQLException {
    pstmt.setBoolean(++pos, v);
  }

  public void setBytes(byte[] v) throws SQLException {
    pstmt.setBytes(++pos, v);
  }

  public void setByte(byte v) throws SQLException {
    pstmt.setByte(++pos, v);
  }

  public void setChar(char v) throws SQLException {
    pstmt.setString(++pos, String.valueOf(v));
  }

  public void setBinaryStream(InputStream inputStream, long length) throws SQLException {
    registerStream(inputStream);
    pstmt.setBinaryStream(++pos, inputStream, length);
  }

  public void setBlob(byte[] bytes) throws SQLException {
    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    pstmt.setBinaryStream(++pos, is, bytes.length);
  }

  public void setClob(String content) throws SQLException {
    Reader reader = new StringReader(content);
    pstmt.setCharacterStream(++pos, reader, content.length());
  }

  public void setArray(String arrayType, Object[] elements) throws SQLException {
    Array array = connection.createArrayOf(arrayType, elements);
    pstmt.setArray(++pos, array);
  }

  /**
   * All streams, that are bound to the prepared statement must be closed.
   * This can't be done on the caller's side, as the stream requires to be open
   * until executeBatch or executeUpdate is invoke. So we track the streams here,
   * i.e. in the batchedPstmt, so that we can close them later.
   */
  private void registerStream(Object obj) {
    if (obj instanceof InputStream) {
      if (batchedPstmt != null) {
        // in batch mode, the BatchPstmt must track the streams.
        batchedPstmt.registerStream((InputStream) obj);
      } else {
        if (streams == null) {
          streams = new ArrayList<>();
        }
        streams.add((InputStream) obj);
      }
    }
  }
}
