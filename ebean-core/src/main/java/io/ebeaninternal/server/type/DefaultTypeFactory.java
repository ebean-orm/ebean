package io.ebeaninternal.server.type;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.JsonConfig;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper to create some default ScalarType objects for Booleans,
 * java.util.Date, java.util.Calendar etc.
 */
final class DefaultTypeFactory {

  private final DatabaseConfig config;

  public DefaultTypeFactory(DatabaseConfig config) {
    this.config = config;
  }

  ScalarTypeBool createBoolean(String trueValue, String falseValue) {
    try {
      // first try Integer based boolean
      Integer intTrue = BasicTypeConverter.toInteger(trueValue);
      Integer intFalse = BasicTypeConverter.toInteger(falseValue);
      return new ScalarTypeBoolean.IntBoolean(intTrue, intFalse);
    } catch (NumberFormatException e) {
      // treat as Varchar/String based boolean
      return new ScalarTypeBoolean.StringBoolean(trueValue, falseValue);
    }
  }

  /**
   * Create the ScalarType for mapping Booleans. For some databases this is a
   * native data type and for others Booleans will be converted to Y/N or 0/1
   * etc.
   */
  public ScalarTypeBool createBoolean() {
    if (config == null) {
      return new ScalarTypeBoolean.Native();
    }
    String trueValue = config.getDatabaseBooleanTrue();
    String falseValue = config.getDatabaseBooleanFalse();
    if (falseValue != null && trueValue != null) {
      // explicit integer or string based booleans
      return createBoolean(trueValue, falseValue);
    }
    // determine based on database platform configuration
    int booleanDbType = config.getDatabasePlatform().getBooleanDbType();
    if (booleanDbType == Types.BIT) {
      return new ScalarTypeBoolean.BitBoolean();
    }
    if (booleanDbType == Types.INTEGER) {
      return new ScalarTypeBoolean.IntBoolean(1, 0);
    }
    if (booleanDbType == Types.VARCHAR) {
      return new ScalarTypeBoolean.StringBoolean("T", "F");
    }
    return new ScalarTypeBoolean.Native();
  }

  /**
   * Create the default ScalarType for java.util.Date.
   */
  public ScalarType<Date> createUtilDate(JsonConfig.DateTime mode, JsonConfig.Date jsonDate) {
    // by default map anonymous java.util.Date to java.sql.Timestamp.
    return createUtilDate(mode, jsonDate, java.sql.Types.TIMESTAMP);
  }

  /**
   * Create a ScalarType for java.util.Date explicitly specifying the type to
   * map to.
   */
  public ScalarType<java.util.Date> createUtilDate(JsonConfig.DateTime jsonDateTime, JsonConfig.Date jsonDate, int utilDateType) {
    switch (utilDateType) {
      case Types.DATE:
        return new ScalarTypeUtilDate.DateType(jsonDate);
      case Types.TIMESTAMP:
        return new ScalarTypeUtilDate.TimestampType(jsonDateTime);
      default:
        throw new RuntimeException("Invalid type " + utilDateType);
    }
  }

  /**
   * Create the default ScalarType for java.util.Calendar.
   */
  public ScalarType<Calendar> createCalendar(JsonConfig.DateTime mode) {
    return createCalendar(mode, java.sql.Types.TIMESTAMP);
  }

  /**
   * Create a ScalarType for java.util.Calendar explicitly specifying the type
   * to map to.
   */
  public ScalarType<Calendar> createCalendar(JsonConfig.DateTime mode, int jdbcType) {
    return new ScalarTypeCalendar(mode, jdbcType);
  }

  /**
   * Create a ScalarType for java.math.BigInteger.
   */
  public ScalarType<BigInteger> createMathBigInteger() {
    return new ScalarTypeMathBigInteger();
  }
}
