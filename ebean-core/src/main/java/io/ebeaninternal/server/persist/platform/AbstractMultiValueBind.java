package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.bind.DataBind;

import java.sql.SQLException;
import java.util.Collection;

import static java.sql.Types.*;

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
    String arrayType = getArrayType(type.jdbcType());
    if (arrayType == null) {
      super.bindMultiValues(dataBind, values, type, bindOne);
    } else {
      dataBind.setArray(arrayType, toArray(values, type));
    }
  }

  String getArrayType(int dbType) {
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
      case ExtraDbTypes.LOCALDATETIME:
        return "timestamp";
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
      case VARBINARY:
        return "bytea";

      default:
        return null;
    }
  }
}
