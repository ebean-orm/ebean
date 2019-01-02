package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebeaninternal.server.type.ScalarType;

/**
 * Multi value binder that uses SqlServers Table-value parameters.
 *
 * It relies that the tvp-types are created by DDL.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class SqlServerMultiValueBind extends AbstractMultiValueBind {


  @Override
  public IsSupported isTypeSupported(int jdbcType) {
    return getArrayType(jdbcType) == null ? IsSupported.NO : IsSupported.ONLY_FOR_MANY_PARAMS;
  }

  @Override
  public void bindMultiValues(int parameterPosition, PreparedStatement pstmt, Collection<?> values, ScalarType<?> type, String tvpName)
      throws SQLException {
    SQLServerDataTable array = new SQLServerDataTable();

    if (ExtraDbTypes.UUID == type.getJdbcType()) {
      array.addColumnMetadata("c1", Types.CHAR); // sqlserver handles uuids as char
    } else {
      array.addColumnMetadata("c1", type.getJdbcType());
    }

    for (Object element : values) {
      if (!type.isJdbcNative()) {
        element = type.toJdbcType(element);
      }
      array.addRow(element);
    }

    SQLServerPreparedStatement sqlserverPstmt = pstmt.unwrap(SQLServerPreparedStatement.class);
    sqlserverPstmt.setStructured(parameterPosition, tvpName, array);
  }

  @Override
  protected String getArrayType(int dbType) {
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
      return null; // NO: Does not work reliable due time zone issues! - Fall back to normal query
      //return "ebean_datetime2_tvp";

    // case LONGVARCHAR:
    // case CLOB:
    case CHAR:
    case VARCHAR:
      // case LONGNVARCHAR:
      // case NCLOB:
    case NCHAR:
    case NVARCHAR:
      return "ebean_nvarchar_tvp";

    case ExtraDbTypes.UUID: // Db Native UUID
      return "ebean_uniqueidentifier_tvp";
    default:
      return null;
    }
  }

  @Override
  protected String getInExpression(boolean not, ScalarType<?> type, int size, String tvpName) {
    if (not) {
      return " not in (SELECT * FROM ?) ";
    } else {
      return " in (SELECT * FROM ?) ";
    }
  }
}
