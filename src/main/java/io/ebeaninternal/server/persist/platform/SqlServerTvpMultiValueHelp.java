package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;
import java.sql.SQLException;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;

/**
 * Multi value binder that uses SqlServers Table-value parameters.
 * 
 * It relies that the tvp-types are created by DDL.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class SqlServerTvpMultiValueHelp extends MultiValueHelp {
  
  
  private static final int MIN_LENGTH = 2;

  @Override
  public void bindMultiValues(Binder binder, DataBind dataBind, Object[] values, int dbType) throws SQLException {
    String tvpName = getTvpName(dbType);
    if (tvpName == null || values.length < MIN_LENGTH) {
      super.bindMultiValues(binder, dataBind, values, dbType);
    } else {
      SQLServerDataTable array = new SQLServerDataTable();
      array.addColumnMetadata("c1", dbType);
      for (Object element: values) {
        array.addRow(element);
      }
     
      SQLServerPreparedStatement sqlserverPstmt = dataBind.getPstmt().unwrap(SQLServerPreparedStatement.class);
      sqlserverPstmt.setStructured(dataBind.nextPos(), tvpName, array);
    }
  }

  private String getTvpName(int dbType) {
    switch(dbType) {
      case TINYINT:
      case SMALLINT:
      case INTEGER:
      case BIGINT:
      case DECIMAL: // TODO: we have no info about precision here
      case NUMERIC:
        return "ebean_bigint_tvp";
      case REAL:
      case FLOAT:
      case DOUBLE:
        return "ebean_float_tvp";
      case BIT:
      case BOOLEAN:
        return "ebean_bit_tvp";
      case DATE:
        return "ebean_date_tvp";
      case TIMESTAMP:
      case TIME_WITH_TIMEZONE:
      case TIMESTAMP_WITH_TIMEZONE:
        return "ebean_datetime2_tvp";
      //case LONGVARCHAR:
      //case CLOB:
      case CHAR:
      case VARCHAR:
      //case LONGNVARCHAR:
      //case NCLOB:
      case NCHAR:
      case NVARCHAR:
        return "ebean_nvarchar_tvp";
      default:
        return null;
    }
  }
  
  @Override
  public void appendInExpression(Binder binder, SpiExpressionRequest request, String propName, boolean not, Object[] values) {
   
    if (values.length < MIN_LENGTH) {
      super.appendInExpression(binder, request, propName, not, values);
    } else {
      int dbType = binder.getScalarType(values[0].getClass()).getJdbcType();
      String tvpName = getTvpName(dbType);
      if (tvpName == null || values.length < MIN_LENGTH) {
        super.appendInExpression(binder, request, propName, not, values);
      } else if (not) {
        request.append(propName).append(" not in (select * from ?) ");
      } else {
        request.append(propName).append(" in (select * from ?) ");
      }
    }
  }

}
