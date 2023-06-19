package io.ebean.config.dbplatform;

import io.ebean.DataBindException;

import java.lang.reflect.Array;
import java.sql.Types;

/**
 * Default implementation of BindValidatorFactory.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class DefaultBindValidatorFactory implements BindValidatorFactory {

  /**
   * Returns true, if the JDBC-type is length based.
   */
  protected boolean isLengthBased(int jdbcType) {
    switch (jdbcType) {
      case Types.BLOB:
      case Types.CLOB:
      case Types.LONGVARBINARY:
      case Types.LONGVARCHAR:
      case Types.VARBINARY:
      case Types.BINARY:
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.NCLOB:
      case Types.LONGNVARCHAR:
      case Types.SQLXML:
      case ExtraDbTypes.JSON:
      case ExtraDbTypes.JSONB:
      case ExtraDbTypes.JSONClob:
      case ExtraDbTypes.JSONBlob:
      case ExtraDbTypes.JSONVarchar:
        return true;
      default:
        return false;
    }
  }

  /**
   * Create BindValidator for length-based properties.
   */
  @Override
  public BindValidator create(PropertyDefinition property) {
    if (property.getDbLength() > 0 && isLengthBased(property.getJdbcType())) {
      return value -> validate(value, property.getDbLength(), property.getTable(), property.getColumn());
    } else {
      return null;
    }
  }

  /**
   * Default validator, that handles length check for String, Arrays, and Files, respectively InputStreamInfo.
   */
  protected void validate(Object value, int dbLength, String table, String column) {
    int valueLength = 0;
    if (value instanceof String) {
      valueLength = ((String) value).length();
    } else if (value instanceof InputStreamInfo) {
      valueLength = (int) ((InputStreamInfo) value).length();
    } else if (value != null && value.getClass().isArray()) {
      valueLength = Array.getLength(value);
    }
    if (valueLength > dbLength) {
      throw new DataBindException("Value of length " + valueLength + " exceeds limit of " + dbLength + " for " + table + "." + column);
    }
  }
}
