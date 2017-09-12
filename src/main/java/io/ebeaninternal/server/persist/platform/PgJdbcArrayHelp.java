package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;
import java.sql.SQLException;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;

/**
 * Multi value binder that uses SqlServers Table-value parameters
 * @author Roland Praml, FOCONIS AG
 *
 */
public class PgJdbcArrayHelp extends MultiValueHelp {


  @Override
  public void bindMultiValues(Binder binder, DataBind dataBind, Object[] values, int dbType) throws SQLException {
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      super.bindMultiValues(binder, dataBind, values, dbType);
    } else {
      dataBind.setArray(getArrayType(dbType), values);
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
      default:
        return null;
    }
  }

  @Override
  public void appendInExpression(Binder binder, SpiExpressionRequest request, String propName, boolean not, Object[] values) {
    int dbType = binder.getScalarType(values[0].getClass()).getJdbcType();
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      super.appendInExpression(binder, request, propName, not, values);
    } else {
      if (not) {
        request.append("not ");
      }
      request.append(propName).append(" = any(?) ");
    }
  }
}
