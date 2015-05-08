package com.avaje.ebeaninternal.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class PreparedStatementDelegator implements PreparedStatement {

  private final PreparedStatement delegate;

  public PreparedStatementDelegator(PreparedStatement delegate) {
    this.delegate = delegate;
  }

  @Override
  public void closeOnCompletion() throws SQLException {
    delegate.closeOnCompletion();
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException {
    return delegate.isCloseOnCompletion();
  }

  public ResultSet executeQuery() throws SQLException {
    return delegate.executeQuery();
  }

  public int executeUpdate() throws SQLException {
    return delegate.executeUpdate();
  }

  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    delegate.setNull(parameterIndex, sqlType);
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    delegate.setBoolean(parameterIndex, x);
  }

  public void setByte(int parameterIndex, byte x) throws SQLException {
    delegate.setByte(parameterIndex, x);
  }

  public void setShort(int parameterIndex, short x) throws SQLException {
    delegate.setShort(parameterIndex, x);
  }

  public void setInt(int parameterIndex, int x) throws SQLException {
    delegate.setInt(parameterIndex, x);
  }

  public void setLong(int parameterIndex, long x) throws SQLException {
    delegate.setLong(parameterIndex, x);
  }

  public void setFloat(int parameterIndex, float x) throws SQLException {
    delegate.setFloat(parameterIndex, x);
  }

  public void setDouble(int parameterIndex, double x) throws SQLException {
    delegate.setDouble(parameterIndex, x);
  }

  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    delegate.setBigDecimal(parameterIndex, x);
  }

  public void setString(int parameterIndex, String x) throws SQLException {
    delegate.setString(parameterIndex, x);
  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    delegate.setBytes(parameterIndex, x);
  }

  public void setDate(int parameterIndex, Date x) throws SQLException {
    delegate.setDate(parameterIndex, x);
  }

  public void setTime(int parameterIndex, Time x) throws SQLException {
    delegate.setTime(parameterIndex, x);
  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    delegate.setTimestamp(parameterIndex, x);
  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    delegate.setAsciiStream(parameterIndex, x, length);
  }

  @SuppressWarnings("deprecation")
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    delegate.setUnicodeStream(parameterIndex, x, length);
  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    delegate.setBinaryStream(parameterIndex, x, length);
  }

  public void clearParameters() throws SQLException {
    delegate.clearParameters();
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    delegate.setObject(parameterIndex, x, targetSqlType);
  }

  public void setObject(int parameterIndex, Object x) throws SQLException {
    delegate.setObject(parameterIndex, x);
  }

  public boolean execute() throws SQLException {
    return delegate.execute();
  }

  public void addBatch() throws SQLException {
    delegate.addBatch();
  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    delegate.setCharacterStream(parameterIndex, reader, length);
  }

  public void setRef(int parameterIndex, Ref x) throws SQLException {
    delegate.setRef(parameterIndex, x);
  }

  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    delegate.setBlob(parameterIndex, x);
  }

  public void setClob(int parameterIndex, Clob x) throws SQLException {
    delegate.setClob(parameterIndex, x);
  }

  public void setArray(int parameterIndex, Array x) throws SQLException {
    delegate.setArray(parameterIndex, x);
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return delegate.getMetaData();
  }

  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    delegate.setDate(parameterIndex, x, cal);
  }

  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    delegate.setTime(parameterIndex, x, cal);
  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    delegate.setTimestamp(parameterIndex, x, cal);
  }

  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    delegate.setNull(parameterIndex, sqlType, typeName);
  }

  public void setURL(int parameterIndex, URL x) throws SQLException {
    delegate.setURL(parameterIndex, x);
  }

  public ParameterMetaData getParameterMetaData() throws SQLException {
    return delegate.getParameterMetaData();
  }

  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    delegate.setRowId(parameterIndex, x);
  }

  public void setNString(int parameterIndex, String value) throws SQLException {
    delegate.setNString(parameterIndex, value);
  }

  public void setNCharacterStream(int parameterIndex, Reader value, long length)
      throws SQLException {
    delegate.setNCharacterStream(parameterIndex, value, length);
  }

  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    delegate.setNClob(parameterIndex, value);
  }

  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    delegate.setClob(parameterIndex, reader, length);
  }

  public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
    delegate.setBlob(parameterIndex, inputStream, length);
  }

  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    delegate.setNClob(parameterIndex, reader, length);
  }

  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    delegate.setSQLXML(parameterIndex, xmlObject);
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
      throws SQLException {
    delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
  }

  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    delegate.setAsciiStream(parameterIndex, x, length);
  }

  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    delegate.setBinaryStream(parameterIndex, x, length);
  }

  public void setCharacterStream(int parameterIndex, Reader reader, long length)
      throws SQLException {
    delegate.setCharacterStream(parameterIndex, reader, length);
  }

  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    delegate.setAsciiStream(parameterIndex, x);
  }

  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    delegate.setBinaryStream(parameterIndex, x);
  }

  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    delegate.setCharacterStream(parameterIndex, reader);
  }

  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    delegate.setNCharacterStream(parameterIndex, value);
  }

  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    delegate.setClob(parameterIndex, reader);
  }

  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    delegate.setBlob(parameterIndex, inputStream);
  }

  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    delegate.setNClob(parameterIndex, reader);
  }

  public ResultSet executeQuery(String sql) throws SQLException {
    return delegate.executeQuery(sql);
  }

  public int executeUpdate(String sql) throws SQLException {
    return delegate.executeUpdate(sql);
  }

  public void close() throws SQLException {
    delegate.close();
  }

  public int getMaxFieldSize() throws SQLException {
    return delegate.getMaxFieldSize();
  }

  public void setMaxFieldSize(int max) throws SQLException {
    delegate.setMaxFieldSize(max);
  }

  public int getMaxRows() throws SQLException {
    return delegate.getMaxRows();
  }

  public void setMaxRows(int max) throws SQLException {
    delegate.setMaxRows(max);
  }

  public void setEscapeProcessing(boolean enable) throws SQLException {
    delegate.setEscapeProcessing(enable);
  }

  public int getQueryTimeout() throws SQLException {
    return delegate.getQueryTimeout();
  }

  public void setQueryTimeout(int seconds) throws SQLException {
    delegate.setQueryTimeout(seconds);
  }

  public void cancel() throws SQLException {
    delegate.cancel();
  }

  public SQLWarning getWarnings() throws SQLException {
    return delegate.getWarnings();
  }

  public void clearWarnings() throws SQLException {
    delegate.clearWarnings();
  }

  public void setCursorName(String name) throws SQLException {
    delegate.setCursorName(name);
  }

  public boolean execute(String sql) throws SQLException {
    return delegate.execute(sql);
  }

  public ResultSet getResultSet() throws SQLException {
    return delegate.getResultSet();
  }

  public int getUpdateCount() throws SQLException {
    return delegate.getUpdateCount();
  }

  public boolean getMoreResults() throws SQLException {
    return delegate.getMoreResults();
  }

  public void setFetchDirection(int direction) throws SQLException {
    delegate.setFetchDirection(direction);
  }

  public int getFetchDirection() throws SQLException {
    return delegate.getFetchDirection();
  }

  public void setFetchSize(int rows) throws SQLException {
    delegate.setFetchSize(rows);
  }

  public int getFetchSize() throws SQLException {
    return delegate.getFetchSize();
  }

  public int getResultSetConcurrency() throws SQLException {
    return delegate.getResultSetConcurrency();
  }

  public int getResultSetType() throws SQLException {
    return delegate.getResultSetType();
  }

  public void addBatch(String sql) throws SQLException {
    delegate.addBatch(sql);
  }

  public void clearBatch() throws SQLException {
    delegate.clearBatch();
  }

  public int[] executeBatch() throws SQLException {
    return delegate.executeBatch();
  }

  public Connection getConnection() throws SQLException {
    return delegate.getConnection();
  }

  public boolean getMoreResults(int current) throws SQLException {
    return delegate.getMoreResults(current);
  }

  public ResultSet getGeneratedKeys() throws SQLException {
    return delegate.getGeneratedKeys();
  }

  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    return delegate.executeUpdate(sql, autoGeneratedKeys);
  }

  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    return delegate.executeUpdate(sql, columnIndexes);
  }

  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    return delegate.executeUpdate(sql, columnNames);
  }

  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    return delegate.execute(sql, autoGeneratedKeys);
  }

  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    return delegate.execute(sql, columnIndexes);
  }

  public boolean execute(String sql, String[] columnNames) throws SQLException {
    return delegate.execute(sql, columnNames);
  }

  public int getResultSetHoldability() throws SQLException {
    return delegate.getResultSetHoldability();
  }

  public boolean isClosed() throws SQLException {
    return delegate.isClosed();
  }

  public void setPoolable(boolean poolable) throws SQLException {
    delegate.setPoolable(poolable);
  }

  public boolean isPoolable() throws SQLException {
    return delegate.isPoolable();
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }
}
