package io.ebean.config.dbplatform;

import io.ebean.DataBindException;

import java.io.File;
import java.lang.reflect.Array;
import java.sql.Types;

/**
 * @author Roland Praml, FOCONIS AG
 */
public class BasicBindValidatorFactory implements BindValidatorFactory {

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

  @Override
  public BindValidator create(PropertyDefinition property) {
    if (property.getDbLength() > 0 && isLengthBased(property.getJdbcType())) {
      return value -> validate(value, property.getDbLength(), property.getTable(), property.getColumn());
    } else {
      return null;
    }
  }

  /**
   * Default validator, that handles length check for String, Arrays, and Files
   */
  protected void validate(Object value, int dbLength, String table, String column) {
    int valueLength = 0;
    if (value instanceof String) {
      valueLength = ((String) value).length();
    } else if (value instanceof File) {
      valueLength = (int) ((File) value).length();
    } else if (value != null && value.getClass().isArray()) {
      valueLength = Array.getLength(value);
    }
    if (valueLength > dbLength) {
      throw new DataBindException("Value of length " + valueLength + " exceeds limit of " + dbLength + " for " + table + "." + column);
    }
  }
}
