package io.ebean.config.dbplatform.sqlserver;

import static java.sql.Types.*;
import java.sql.SQLException;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;

import io.ebean.config.dbplatform.MultiValueBinder;
import io.ebeaninternal.server.type.DataBind;

/**
 * Multi value binder that uses SqlServers Table-value parameters
 * @author Roland Praml, FOCONIS AG
 *
 */
public class SqlServerTVPMultiValueBinder implements MultiValueBinder {


  
  @Override
  public void bindObjects(DataBind dataBind, Object[] values, int dbType) throws SQLException{
    SQLServerDataTable array = new SQLServerDataTable();
    array.addColumnMetadata("c1", dbType);
    for (Object element: values) {
      array.addRow(element);
    }
   
    SQLServerPreparedStatement sqlserverPstmt = dataBind.getPstmt().unwrap(SQLServerPreparedStatement.class);
    sqlserverPstmt.setStructured(dataBind.nextPos(), getTvpName(dbType), array);
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
      case LONGVARCHAR:
      case CLOB:
      case CHAR:
      case VARCHAR:
      case LONGNVARCHAR:
      case NCLOB:
      case NCHAR:
      case NVARCHAR:
        return "ebean_nvarchar_tvp";
      default:
        throw new IllegalArgumentException("Cannot map jdbctype '" + dbType + "'  to TVP");
    }
  }


  @Override
  public String getPlaceholder(int length) {
    return "select * from ?";
  }

}
