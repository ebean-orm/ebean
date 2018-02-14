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
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class SqlServerMultiValueBind extends MultiValueBind {

  private static final int MIN_LENGTH = 2;

  @Override
  public void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne)
      throws SQLException {
    String tvpName = getTvpName(type.getJdbcType());
    if (tvpName == null || values.size() < MIN_LENGTH) {
      super.bindMultiValues(dataBind, values, type, bindOne);
    } else {
      SQLServerDataTable array = new SQLServerDataTable();
      array.addColumnMetadata("c1", type.getJdbcType());
      for (Object element : values) {
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
    switch (dbType) {
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
    // case LONGVARCHAR:
    // case CLOB:
    case CHAR:
    case VARCHAR:
      // case LONGNVARCHAR:
      // case NCLOB:
    case NCHAR:
    case NVARCHAR:
      return "ebean_nvarchar_tvp";
    default:
      return null;
    }
  }

  @Override
  public String getInExpression(boolean not, ScalarType<?> type, int size) {
    if (size < MIN_LENGTH) {
      return super.getInExpression(not, type, size);
    } else {
      String tvpName = getTvpName(type.getJdbcType());
      if (tvpName == null) {
        return super.getInExpression(not, type, size);
      } else {
        return " IN (SELECT * FROM ?) ";
      }
    }
  }

}
