package com.avaje.ebeaninternal.server.type;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public interface DataReader {

  void close() throws SQLException;

  boolean next() throws SQLException;

  void resetColumnPosition();

  void incrementPos(int increment);

  byte[] getBinaryBytes() throws SQLException;

  String getStringFromStream() throws SQLException;

  String getString() throws SQLException;

  Boolean getBoolean() throws SQLException;

  Byte getByte() throws SQLException;

  Short getShort() throws SQLException;

  Integer getInt() throws SQLException;

  Long getLong() throws SQLException;

  Float getFloat() throws SQLException;

  Double getDouble() throws SQLException;

  byte[] getBytes() throws SQLException;

  Date getDate() throws SQLException;

  Time getTime() throws SQLException;

  Timestamp getTimestamp() throws SQLException;

  BigDecimal getBigDecimal() throws SQLException;

  Array getArray() throws SQLException;

  Object getObject() throws SQLException;

  InputStream getBinaryStream() throws SQLException;
}
