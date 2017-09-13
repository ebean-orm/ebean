package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;
import java.sql.SQLException;

import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;

/**
 * Multi value binder for DB2
 * TODO: THIS DOES NOT WORK YET!
 * @author Roland Praml, FOCONIS AG
 *
 */
public class Db2JdbcArrayHelp extends MultiValueHelp {


  @Override
  public void bindMultiValues(Binder binder, DataBind dataBind, Object[] values, int dbType) throws SQLException {
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      super.bindMultiValues(binder, dataBind, values, dbType);
    } else {
      String[] sValues = new String[values.length];
      for(int i = 0; i < values.length; i++) {
        sValues[i] = values[i].toString();
      }
      dataBind.setArray(getArrayType(dbType), sValues);
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
        return "MYARR";
      default:
        return null;
    }
  }

  @Override
  public String getInExpression(Binder binder,  boolean not, Object[] values) {
    int dbType = binder.getScalarType(values[0].getClass()).getJdbcType();
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      return super.getInExpression(binder, not, values);
    } else {
      if (not) {
        return " not in (?) ";
      } else {
        return " in (?) ";
      }
    }
  }
}
