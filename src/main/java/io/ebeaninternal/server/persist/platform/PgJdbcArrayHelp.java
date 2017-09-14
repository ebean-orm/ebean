package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;
import java.sql.SQLException;
import java.util.Collection;

import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.ScalarType;

/**
 * Multi value binder that uses SqlServers Table-value parameters
 * @author Roland Praml, FOCONIS AG
 *
 */
public class PgJdbcArrayHelp extends MultiValueHelp {


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
        return "BIGINT";
      case REAL:
      case FLOAT:
      case DOUBLE:
        return "FLOAT";
      case BIT:
      case BOOLEAN:
        return "BIT";
      case DATE:
        return "DATE";
      case TIMESTAMP:
      case TIME_WITH_TIMEZONE:
      case TIMESTAMP_WITH_TIMEZONE:
        return "TIMESTAMP";
      //case LONGVARCHAR:
      //case CLOB:
      case CHAR:
      case VARCHAR:
      //case LONGNVARCHAR:
      //case NCLOB:
      case NCHAR:
      case NVARCHAR:
        return "VARCHAR";
      default:
        return null;
    }
  }

  @Override
  public String getInExpression(ScalarType<?> type, int size) {
    String arrayType = getArrayType(type.getJdbcType());
    if (arrayType == null) {
      return super.getInExpression(type, size);
    } else {
      return " IN (SELECT(UNNEST(?))) ";
    }
  }
}
