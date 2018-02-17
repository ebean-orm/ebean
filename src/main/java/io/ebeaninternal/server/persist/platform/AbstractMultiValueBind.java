package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.SQLException;
import java.util.Collection;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NCHAR;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.NVARCHAR;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TIMESTAMP_WITH_TIMEZONE;
import static java.sql.Types.TIME_WITH_TIMEZONE;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;

/**
 * Base MultiValueBind for platform specific support.
 */
abstract class AbstractMultiValueBind extends MultiValueBind {

  // FIXME: Rückbau, das klappt nicht bei IDs!
  protected int minLength = 0; // only when we have at least 2 values, it is worth to use the statement.

  @Override
  public boolean isSupported(int valueCount) {
    return valueCount >= minLength;
  }

  @Override
  public boolean isTypeSupported(int jdbcType) {
    return getArrayType(jdbcType) != null;
  }

  @Override
  public final void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne) throws SQLException {
    if (!isSupported(values.size())) {
      super.bindMultiValues(dataBind, values, type, bindOne);
    } else {
      String arrayType = getArrayType(type.getJdbcType());
      if (arrayType == null) {
        super.bindMultiValues(dataBind, values, type, bindOne);
      } else {
        bindMultiValues(dataBind, values, type, bindOne, arrayType);
      }
    }
  }

  /**
   * Bind the values if MultiValueBind can be used. Overwrite this method.
   */
  protected void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne, String arrayType) throws SQLException {
    dataBind.setArray(arrayType, toArray(values, type));
  }

  @Override
  public final String getInExpression(boolean not, ScalarType<?> type, int size) {
    if (!isSupported(size)) {
      return super.getInExpression(not, type, size);
    } else {
      String arrayType = getArrayType(type.getJdbcType());
      if (arrayType == null) {
        return super.getInExpression(not, type, size);
      } else {
        return getInExpression(not, type, size, arrayType);
      }
    }
  }

  /**
   * Appends the 'in' expression to the request. Must add leading & trailing space! Overweite this method.
   */
  protected abstract String getInExpression(boolean not, ScalarType<?> type, int size, String arrayType);

  protected String getArrayType(int dbType) {
    switch(dbType) {
      case TINYINT:
      case SMALLINT:
      case INTEGER:
      case BIGINT:
      case DECIMAL: // TODO: we have no info about precision here
      case NUMERIC:
        return "bigint";
      case REAL:
      case FLOAT:
      case DOUBLE:
        return "float";
      case BIT:
      case BOOLEAN:
        return "bit";
      case DATE:
        return "date";
      case TIMESTAMP:
      case TIME_WITH_TIMEZONE:
      case TIMESTAMP_WITH_TIMEZONE:
        return "timestamp";
      //case LONGVARCHAR:
      //case CLOB:
      case CHAR:
      case VARCHAR:
        //case LONGNVARCHAR:
        //case NCLOB:
      case NCHAR:
      case NVARCHAR:
        return "varchar";
      case ExtraDbTypes.UUID: // Db Native UUID
        return "varchar";

      default:
        return null;
    }
  }
}
