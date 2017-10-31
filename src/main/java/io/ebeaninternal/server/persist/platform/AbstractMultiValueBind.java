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

  @Override
  public boolean isSupported() {
    return true;
  }

  @Override
  public boolean isTypeSupported(int jdbcType) {
    return getArrayType(jdbcType) != null;
  }

  @Override
  public void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne) throws SQLException {
    String arrayType = getArrayType(type.getJdbcType());
    if (arrayType == null) {
      super.bindMultiValues(dataBind, values, type, bindOne);
    } else {
      dataBind.setArray(arrayType, toArray(values, type));
    }
  }

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
