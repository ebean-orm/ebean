package io.ebean.core.type;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

/**
 * Data binder for ScalarTypes generally to an underlying PreparedStatement.
 */
public interface DataBinder {

  /**
   * Add something to the binding log.
   */
  StringBuilder append(Object entry);

  /**
   * Return the binding log.
   */
  StringBuilder log();

  /**
   * Close the underlying prepared statement.
   */
  void close() throws SQLException;

  /**
   * Return the current position. Effectively column binding position.
   */
  int currentPos();

  /**
   * Return the next position.
   */
  int nextPos();

  /**
   * Decrement the position.
   */
  void decrementPos();

  /**
   * Execute as a dml statement.
   */
  int executeUpdate() throws SQLException;

  /**
   * Return the underlying PreparedStatement.
   */
  PreparedStatement getPstmt();

  /**
   * Return any inputStreams that have been bound (and should be closed).
   * This is used for batched statement execution only.
   */
  List<InputStream> getInputStreams();

  /**
   * Bind an object.
   */
  void setObject(Object value) throws SQLException;

  /**
   * Bind an object with given sql type.
   */
  void setObject(Object value, int sqlType) throws SQLException;

  /**
   * Bind null.
   */
  void setNull(int jdbcType) throws SQLException;

  /**
   * Bind a string value.
   */
  void setString(String value) throws SQLException;

  /**
   * Bind a int value.
   */
  void setInt(int value) throws SQLException;

  /**
   * Bind a long value.
   */
  void setLong(long value) throws SQLException;

  /**
   * Bind a short value.
   */
  void setShort(short value) throws SQLException;

  /**
   * Bind a float value.
   */
  void setFloat(float value) throws SQLException;

  /**
   * Bind a double value.
   */
  void setDouble(double value) throws SQLException;

  /**
   * Bind a BigDecimal value.
   */
  void setBigDecimal(BigDecimal value) throws SQLException;

  /**
   * Bind a date value.
   */
  void setDate(java.sql.Date value) throws SQLException;

  /**
   * Bind a timestamp value.
   */
  void setTimestamp(Timestamp value) throws SQLException;

  /**
   * Bind a time value.
   */
  void setTime(Time value) throws SQLException;

  /**
   * Bind a boolean value.
   */
  void setBoolean(boolean value) throws SQLException;

  /**
   * Bind a byte array value.
   */
  void setBytes(byte[] value) throws SQLException;

  /**
   * Bind a byte value.
   */
  void setByte(byte value) throws SQLException;

  /**
   * Bind a char value.
   */
  void setChar(char value) throws SQLException;

  /**
   * Bind a InputStream value.
   */
  void setBinaryStream(InputStream inputStream, long length) throws SQLException;

  /**
   * Bind a byte array value.
   */
  void setBlob(byte[] bytes) throws SQLException;

  /**
   * Bind a string clob value.
   */
  void setClob(String content) throws SQLException;

  /**
   * Bind an array value.
   */
  void setArray(String arrayType, Object[] elements) throws SQLException;
}
