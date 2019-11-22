package io.ebeaninternal.server.type.bindcapture;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

class BindCaptureTypes {

  static class Null implements BindCaptureEntry {

    private final int parameterIndex;
    private final int sqlType;

    Null(int parameterIndex, int sqlType) {
      this.parameterIndex = parameterIndex;
      this.sqlType = sqlType;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setNull(parameterIndex, sqlType);
    }

    @Override
    public String toString() {
      return "null";
    }
  }

  static class Boolean implements BindCaptureEntry {

    private final int parameterIndex;
    private final boolean x;

    Boolean(int parameterIndex, boolean x) {
      this.parameterIndex = parameterIndex;
      this.x = x;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setBoolean(parameterIndex, x);
    }

    @Override
    public String toString() {
      return String.valueOf(x);
    }
  }

  static class Byte implements BindCaptureEntry {

    private final int parameterIndex;
    private final byte x;

    Byte(int parameterIndex, byte x) {
      this.parameterIndex = parameterIndex;
      this.x = x;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setByte(parameterIndex, x);
    }

    @Override
    public String toString() {
      return String.valueOf(x);
    }
  }

  static class Bytes implements BindCaptureEntry {

    private final int parameterIndex;
    private final byte[] x;

    Bytes(int parameterIndex, byte[] x) {
      this.parameterIndex = parameterIndex;
      this.x = x;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setBytes(parameterIndex, x);
    }

    @Override
    public String toString() {
      return Arrays.toString(x);
    }
  }

  static class TShort implements BindCaptureEntry {

    private final int parameterIndex;
    private final short value;

    TShort(int parameterIndex, short value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setShort(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TInt implements BindCaptureEntry {

    private final int parameterIndex;
    private final int value;

    TInt(int parameterIndex, int value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setInt(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TLong implements BindCaptureEntry {

    private final int parameterIndex;
    private final long value;

    TLong(int parameterIndex, long value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setLong(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TFloat implements BindCaptureEntry {

    private final int parameterIndex;
    private final float value;

    TFloat(int parameterIndex, float value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setFloat(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TDouble implements BindCaptureEntry {

    private final int parameterIndex;
    private final double value;

    TDouble(int parameterIndex, double value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setDouble(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TBigDecimal implements BindCaptureEntry {

    private final int parameterIndex;
    private final BigDecimal value;

    TBigDecimal(int parameterIndex, BigDecimal value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setBigDecimal(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }


  static class TString implements BindCaptureEntry {

    private final int parameterIndex;
    private final String value;

    TString(int parameterIndex, String value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setString(parameterIndex, value);
    }

    @Override
    public String toString() {
      return value;
    }
  }

  static class TDate implements BindCaptureEntry {

    private final int parameterIndex;
    private final Date value;

    TDate(int parameterIndex, Date value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setDate(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TTime implements BindCaptureEntry {

    private final int parameterIndex;
    private final Time value;

    TTime(int parameterIndex, Time value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setTime(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TTimestamp implements BindCaptureEntry {

    private final int parameterIndex;
    private final Timestamp value;
    private final Calendar timezone;

    TTimestamp(int parameterIndex, Timestamp value, Calendar timezone) {
      this.parameterIndex = parameterIndex;
      this.value = value;
      this.timezone = timezone;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      if (timezone == null) {
        statement.setTimestamp(parameterIndex, value);
      } else {
        statement.setTimestamp(parameterIndex, value, timezone);
      }
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TObject implements BindCaptureEntry {

    private final int parameterIndex;
    private final Object value;

    TObject(int parameterIndex, Object value) {
      this.parameterIndex = parameterIndex;
      this.value = value;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setObject(parameterIndex, value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  static class TArray implements BindCaptureEntry {

    private final int parameterIndex;
    private final String arrayType;
    private final Object[] elements;


    TArray(int parameterIndex, String arrayType, Object[] elements) {
      this.parameterIndex = parameterIndex;
      this.arrayType = arrayType;
      this.elements = elements;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      java.sql.Array array = connection.createArrayOf(arrayType, elements);
      statement.setArray(parameterIndex, array);
    }

    @Override
    public String toString() {
      return "Array{" + arrayType + ": " + Arrays.toString(elements) + "}";
    }
  }

  static class CharacterStream implements BindCaptureEntry {

    private static final String dummy = "hi";

    private final int parameterIndex;

    CharacterStream(int parameterIndex) {
      this.parameterIndex = parameterIndex;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setCharacterStream(parameterIndex, new StringReader(dummy), dummy.length());
    }

    @Override
    public String toString() {
      return "charStream";
    }
  }

  static class BinaryStream implements BindCaptureEntry {

    private static final byte[] dummy = "hi".getBytes(Charset.defaultCharset());

    private final int parameterIndex;

    BinaryStream(int parameterIndex) {
      this.parameterIndex = parameterIndex;
    }

    @Override
    public void bind(PreparedStatement statement, Connection connection) throws SQLException {
      statement.setBinaryStream(parameterIndex, new ByteArrayInputStream(dummy), dummy.length);
    }

    @Override
    public String toString() {
      return "binaryStream";
    }
  }
}
