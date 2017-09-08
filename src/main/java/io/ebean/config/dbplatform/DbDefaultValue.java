package io.ebean.config.dbplatform;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import io.ebean.annotation.DbDefault;

/**
 * DB Column default values mapping to database platform specific literals.
 */
public class DbDefaultValue {

  /**
   * The key for FALSE.
   */
  public static final String FALSE = "false";

  /**
   * The key for TRUE.
   */
  public static final String TRUE = "true";

  /**
   * The key for the NOW / current timestamp.
   */
  public static final String NOW = "now";
  
  /**
   * The 'null' literal.
   */
  public static final String NULL = "null";
  


  protected Map<String, String> map = new LinkedHashMap<>();

  /**
   * Set the DB now function.
   */
  public void setNow(String dbFunction) {
    put(NOW, dbFunction);
  }

  /**
   * Set the DB false literal.
   */
  public void setFalse(String dbFalseLiteral) {
    put(FALSE, dbFalseLiteral);
  }

  /**
   * Set the DB true literal.
   */
  public void setTrue(String dbTrueLiteral) {
    put(TRUE, dbTrueLiteral);
  }

  /**
   * Add an translation entry.
   */
  public void put(String dbLiteral, String dbTranslated) {
    map.put(dbLiteral, dbTranslated);
  }

  /**
   * Convert the DB default literal to platform specific type or function.
   * <p>
   * This is intended for the DB column default clause in DDL.
   * </p>
   */
  public String convert(String dbDefaultLiteral) {
    if (dbDefaultLiteral == null) {
      return null;
    }
    if (dbDefaultLiteral.startsWith("$RAW:")) {
      return dbDefaultLiteral.substring(5);
    }
    String val = map.get(dbDefaultLiteral);
    return val != null ? val : dbDefaultLiteral;
  }

  
  /**
   * This method checks & convert the {@link DbDefault#value()} to a valid SQL literal.
   * 
   * This is mainly to quote string literals and verify integer/dates for correctness.
   * <p>
   * Note: There are some special cases:
   * </p>
   * <ul>
   *    <li>Normal Quoting: <code>@DbDefault("User's default")</code> on a String propery
   *        returns: <code>default 'User''s default'</code><br/>
   *        (the same on an integer property will throw a NumberFormatException)</li>
   *    <li>Special case null: <code>@DbDefault("null")</code> will return this: <code>default null</code><br/>
   *        If you need really the String "null", you have to specify <code>@DbDefault("'null'")</code>
   *        which gives you the <code>default 'null'</code> statement.</li>
   *    <li>Any statement, that begins and ends with single quote will not be checked or get quoted again.</li>
   *    <li>A statement that begins with "$RAW:", e.g <code>@DbDefault("$RAW:N'SANDNES'")</code> will lead to 
   *        a <code>default N'SANDNES'</code> in DDL. Note that this is platform specific!</li>
   * </ul>
   */
  public static String toSqlLiteral(String defaultValue, Class<?> propertyType, int sqlType) {
    if (propertyType == null
        || defaultValue == null 
        || NULL.equals(defaultValue)
        || (defaultValue.startsWith("'") && defaultValue.endsWith("'"))
        || (defaultValue.startsWith("$RAW:"))) {  
      return defaultValue;
    }

    if (Boolean.class.isAssignableFrom(propertyType) || Boolean.TYPE.isAssignableFrom(propertyType)) {
      return toBooleanLiteral(defaultValue);
    }
    
    if (Number.class.isAssignableFrom(propertyType) 
        || Byte.TYPE.equals(propertyType)
        || Short.TYPE.equals(propertyType)
        || Integer.TYPE.equals(propertyType)
        || Long.TYPE.equals(propertyType)
        || Float.TYPE.equals(propertyType)
        || Double.TYPE.equals(propertyType)
        || (propertyType.isEnum() && sqlType == Types.INTEGER)) {
      Double.valueOf(defaultValue); // verify if it is a number
      return defaultValue;
    }
   
    // check if it is a date/time - in all other cases return quoted defaultValue
    switch (sqlType) {
      // date
      case Types.DATE:
        return toDateLiteral(defaultValue);
      // time
      case Types.TIME:
      case Types.TIME_WITH_TIMEZONE:
        return toTimeLiteral(defaultValue);
      // timestamp
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return toDateTimeLiteral(defaultValue);

      default:
        return toTextLiteral(defaultValue); // do not check other datatypes
    }
  }

  /**
   * Checks if specified value is either 'true' or 'false'. The literal is translated later.
   */
  private static String toBooleanLiteral(String value) {
    if (DbDefaultValue.FALSE.equals(value) || DbDefaultValue.TRUE.equals(value)) {
      return value;
    }
    throw new IllegalArgumentException("'" + value + "' is not a valid value for boolean");
  }
  
  /**
   * This adds single qoutes around the <code>value</code> and doubles single quotes.
   * "User's home" will return "'User''s home'"
   */
  private static String toTextLiteral(String value) {
    StringBuilder sb = new StringBuilder(value.length()+10);
    sb.append('\'');
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (ch == '\'') {
        sb.append("''");
      } else {
        sb.append(ch);
      }
    }
    sb.append('\'');
    return sb.toString();

  }
  
  private static String toDateLiteral(String value) {
    if (NOW.equals(value)) {
      return value; // this will get translated later
    }
    DatatypeConverter.parseDate(value); // verify
    return toTextLiteral(value);
  }

  private static String toTimeLiteral(String value) {
    if (NOW.equals(value)) {
      return value; // this will get translated later
    }
    DatatypeConverter.parseTime(value); // verify
    return toTextLiteral(value);
  }
  
  private static String toDateTimeLiteral(String value) {
    if (NOW.equals(value)) {
      return value; // this will get translated later
    }
    DatatypeConverter.parseDateTime(value); // verify
    return toTextLiteral(value);
  }
}
