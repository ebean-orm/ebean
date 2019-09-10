package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Boolean and boolean.
 * <p>
 * This may or may not be a native jdbc type depending on the database and jdbc driver.
 * </p>
 */
public class ScalarTypeBoolean {

  public static class Native extends BooleanBase {

    /**
     * Native Boolean database type.
     */
    public Native() {
      super(true, Types.BOOLEAN);
    }

    @Override
    public String getDbFalseLiteral() {
      return "false";
    }

    @Override
    public String getDbTrueLiteral() {
      return "true";
    }

    @Override
    public Boolean toBeanType(Object value) {
      return BasicTypeConverter.toBoolean(value);
    }

    @Override
    public Object toJdbcType(Object value) {
      return BasicTypeConverter.convert(value, jdbcType);
    }

    @Override
    public void bind(DataBind b, Boolean value) throws SQLException {
      if (value == null) {
        b.setNull(Types.BOOLEAN);
      } else {
        b.setBoolean(value);
      }

    }

    @Override
    public Boolean read(DataReader dataReader) throws SQLException {
      return dataReader.getBoolean();
    }
  }

  /**
   * The Class BitBoolean converts a JDBC type BIT to a java boolean
   * <p/>
   * <p>
   * Sometimes booleans may be mapped to the JDBC type BIT. To use the BitBoolean specify
   * type.boolean.dbtype="bit" in the ebean configuration
   * </p>
   */
  static class BitBoolean extends BooleanBase {

    /**
     * Native Boolean database type.
     */
    BitBoolean() {
      super(true, Types.BIT);
    }

    @Override
    public String getDbFalseLiteral() {
      return "0";
    }

    @Override
    public String getDbTrueLiteral() {
      return "1";
    }

    @Override
    public Boolean toBeanType(Object value) {
      return BasicTypeConverter.toBoolean(value);
    }

    @Override
    public Object toJdbcType(Object value) {
      // use JDBC driver to convert boolean to bit
      return BasicTypeConverter.toBoolean(value);
    }

    @Override
    public void bind(DataBind b, Boolean value) throws SQLException {
      if (value == null) {
        b.setNull(Types.BIT);
      } else {
        // use JDBC driver to convert boolean to bit
        b.setBoolean(value);
      }
    }

    @Override
    public Boolean read(DataReader dataReader) throws SQLException {
      return dataReader.getBoolean();
    }

  }

  /**
   * Converted to/from an Integer in the Database.
   */
  static class IntBoolean extends BooleanBase {

    private final Integer trueValue;
    private final Integer falseValue;

    IntBoolean(Integer trueValue, Integer falseValue) {
      super(false, Types.INTEGER);
      this.trueValue = trueValue;
      this.falseValue = falseValue;
    }

    @Override
    public String getDbFalseLiteral() {
      return falseValue.toString();
    }

    @Override
    public String getDbTrueLiteral() {
      return trueValue.toString();
    }

    @Override
    public int getLength() {
      return 1;
    }

    @Override
    public void bind(DataBind b, Boolean value) throws SQLException {
      if (value == null) {
        b.setNull(Types.INTEGER);
      } else {
        b.setInt(toInteger(value));
      }
    }

    @Override
    public Boolean read(DataReader dataReader) throws SQLException {
      Integer i = dataReader.getInt();
      if (i == null) {
        return null;
      }
      if (i.equals(trueValue)) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }

    @Override
    public Object toJdbcType(Object value) {
      return toInteger(value);
    }

    /**
     * Convert the Boolean value to the db value.
     */
    Integer toInteger(Object value) {
      if (value == null) {
        return null;
      }
      Boolean b = (Boolean) value;
      return b ? trueValue : falseValue;
    }

    /**
     * Convert the db value to the Boolean value.
     */
    @Override
    public Boolean toBeanType(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Boolean) {
        return (Boolean) value;
      }
      if (trueValue.equals(value)) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }

  }

  /**
   * Converted to/from an Integer in the Database.
   */
  static class StringBoolean extends BooleanBase {

    private final String trueValue;
    private final String falseValue;

    StringBoolean(String trueValue, String falseValue) {
      super(false, Types.VARCHAR);
      this.trueValue = trueValue;
      this.falseValue = falseValue;
    }

    @Override
    public String getDbFalseLiteral() {
      return "'" + falseValue + "'";
    }

    @Override
    public String getDbTrueLiteral() {
      return "'" + trueValue + "'";
    }

    @Override
    public int getLength() {
      // typically this will return 1
      return Math.max(trueValue.length(), falseValue.length());
    }

    @Override
    public void bind(DataBind b, Boolean value) throws SQLException {
      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else {
        b.setString(toString(value));
      }
    }

    @Override
    public Boolean read(DataReader dataReader) throws SQLException {
      String string = dataReader.getString();
      if (string == null) {
        return null;
      }

      if (string.equals(trueValue)) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }

    @Override
    public Object toJdbcType(Object value) {
      return toString(value);
    }

    /**
     * Convert the Boolean value to the db value.
     */
    public String toString(Object value) {
      if (value == null) {
        return null;
      }
      Boolean b = (Boolean) value;
      return b ? trueValue : falseValue;
    }

    /**
     * Convert the db value to the Boolean value.
     */
    @Override
    public Boolean toBeanType(Object value) {
      if (value == null) {
        return null;
      }
      if (value instanceof Boolean) {
        return (Boolean) value;
      }
      if (trueValue.equals(value)) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }
  }

  public static abstract class BooleanBase extends ScalarTypeBase<Boolean> implements ScalarTypeBool {

    BooleanBase(boolean jdbcNative, int jdbcType) {
      super(Boolean.class, jdbcNative, jdbcType);
    }

    @Override
    public int getLogicalType() {
      return Types.BOOLEAN;
    }

    /**
     * Return the DB literal value for false.
     */
    @Override
    public abstract String getDbFalseLiteral();

    /**
     * Return the DB literal value for true.
     */
    @Override
    public abstract String getDbTrueLiteral();

    @Override
    public String formatValue(Boolean t) {
      return t.toString();
    }

    @Override
    public Boolean parse(String value) {
      return Boolean.valueOf(value);
    }

    @Override
    public Boolean convertFromMillis(long systemTimeMillis) {
      throw new TextException("Not Supported");
    }

    @Override
    public boolean isDateTimeCapable() {
      return false;
    }

    @Override
    public Boolean readData(DataInput dataInput) throws IOException {
      if (!dataInput.readBoolean()) {
        return null;
      } else {
        return dataInput.readBoolean();
      }
    }

    @Override
    public void writeData(DataOutput dataOutput, Boolean val) throws IOException {

      if (val == null) {
        dataOutput.writeBoolean(false);
      } else {
        dataOutput.writeBoolean(true);
        dataOutput.writeBoolean(val);
      }
    }

    @Override
    public Boolean jsonRead(JsonParser parser) {
      return JsonToken.VALUE_TRUE == parser.getCurrentToken() ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public void jsonWrite(JsonGenerator writer, Boolean value) throws IOException {
      writer.writeBoolean(value);
    }

    @Override
    public DocPropertyType getDocType() {
      return DocPropertyType.BOOLEAN;
    }
  }

}
