package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.DataBindCapture;
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

import java.sql.Array;
import java.sql.PreparedStatement;

/**
 * Base MultiValueBind for platform specific support.
 */
public abstract class AbstractMultiValueBind extends MultiValueBind {

  @Override
  public final void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, Binder binder) throws SQLException {

    switch (isTypeSupported(type.getJdbcType())) {
    case NO:
      super.bindMultiValues(dataBind, values, type, binder);
      break;
    case ONLY_FOR_MANY_PARAMS:
      if (values.size() <= MANY_PARAMS) {
        super.bindMultiValues(dataBind, values, type, binder);
        break;
      }
      // fall thru
    case YES:
      String arrayType = getArrayType(type.getJdbcType());
      if (dataBind instanceof DataBindCapture) {
        ((DataBindCapture)dataBind).bindMultiValues(values, type, arrayType, this);
      } else {
        bindMultiValues(dataBind.nextPos(), dataBind.getPstmt(), values, type, arrayType);
      }
      break;
    default:
      break;
    }
  }

  /**
   * Bind the values if MultiValueBind can be used. Overwrite this method.
   */
  public void bindMultiValues(int parameterPosition, PreparedStatement pstmt, Collection<?> values, ScalarType<?> type, String arrayType) throws SQLException {
    Array array = pstmt.getConnection().createArrayOf(arrayType, toArray(values, type));
    pstmt.setArray(parameterPosition, array);
  }

  @Override
  public final String getInExpression(boolean not, ScalarType<?> type, int size) {
    switch (isTypeSupported(type.getJdbcType())) {
    case NO:
      return super.getInExpression(not, type, size);
    case ONLY_FOR_MANY_PARAMS:
      if (size <= MANY_PARAMS) {
        return super.getInExpression(not, type, size);
      }
      // fall thru
    case YES:
      return getInExpression(not, type, size, getArrayType(type.getJdbcType()));
    default:
      throw new IllegalStateException();
    }
  }

  /**
   * Appends the 'in' expression to the request. Must add leading and trailing space! Overweite this method.
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
        return null; // NO: Does not work reliable due time zone issues! - Fall back to normal query
        //return "timestamp";
      //case LONGVARCHAR:
      //case CLOB:
      case CHAR:
      case VARCHAR:
        //case LONGNVARCHAR:
        //case NCLOB:
      case NCHAR:
      case NVARCHAR:
      case ExtraDbTypes.UUID: // Postgres cast to uuid[]
      case ExtraDbTypes.INET: // Postgres cast to inet[]
        return "varchar";

      default:
        return null;
    }
  }
}
