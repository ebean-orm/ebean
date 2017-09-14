package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;
import java.sql.SQLException;
import java.util.Collection;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;

import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.ScalarType;

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
  public void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne) throws SQLException {
    String tvpName = getTvpName(type.getJdbcType());
    if (tvpName == null || values.size() < MIN_LENGTH) {
      super.bindMultiValues(dataBind, values, type, bindOne);
    } else {
      SQLServerDataTable array = new SQLServerDataTable();
      array.addColumnMetadata("c1", type.getJdbcType());
      for (Object element: values) {
        if (!type.isJdbcNative()) {
          element = type.toJdbcType(element);
        }
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
        return "EBEAN_BIGINT_TVP";
      case REAL:
      case FLOAT:
      case DOUBLE:
        return "EBEAN_FLOAT_TVP";
      case BIT:
      case BOOLEAN:
        return "EBEAN_BIT_TVP";
      case DATE:
        return "EBEAN_DATE_TVP";
      case TIMESTAMP:
      case TIME_WITH_TIMEZONE:
      case TIMESTAMP_WITH_TIMEZONE:
        return "EBEAN_DATETIME2_TVP";
      //case LONGVARCHAR:
      //case CLOB:
      case CHAR:
      case VARCHAR:
      //case LONGNVARCHAR:
      //case NCLOB:
      case NCHAR:
      case NVARCHAR:
        return "EBEAN_NVARCHAR_TVP";
      default:
        return null;
    }
  }
  
  @Override
  public String getInExpression(ScalarType<?> type, int size) {
   
    if (size < MIN_LENGTH) {
      return super.getInExpression(type, size);
    } else {
      String tvpName = getTvpName(type.getJdbcType());
      if (tvpName == null || size < MIN_LENGTH) {
        return super.getInExpression(type, size);
      } else {
        return" IN (SELECT * FROM ?) ";
      }
    }
  }

}
