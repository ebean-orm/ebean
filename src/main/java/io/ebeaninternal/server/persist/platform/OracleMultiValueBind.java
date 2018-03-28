package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.ScalarType;

// import oracle.jdbc.*

/**
 * Multi value binder that uses oracle's createOracleArray.
 *
 * Unfortunately, oracle did not implement createArray and if we import
 * oracle.jdbc, we need the driver to compile ebean. The driver is not freely
 * available, that's why we have to use some reflection to access this method.
 *
 * It relies that the tvp-types are created by DDL.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class OracleMultiValueBind extends AbstractMultiValueBind {

  interface OracleHelp {
    Array createArray(Connection conn, String tvpName, Object[] values) throws SQLException;
  }
  // -------------
  private static OracleHelp ORACLE_HELP;
  {
    try {
      ORACLE_HELP = (OracleHelp) OracleMultiValueBind.class.forName("io.ebeaninternal.server.persist.platform.OracleHelpImpl").newInstance();
    } catch (Exception e){
      ORACLE_HELP = null;
      e.printStackTrace();
    }
  }

  @Override
  public IsSupported isTypeSupported(int jdbcType) {
    return getArrayType(jdbcType) == null ? IsSupported.NO : IsSupported.ONLY_FOR_MANY_PARAMS;
  }

  // code without reflection
  // private Array createArray(Connection conn, String tvpName, Object[] values)
  // throws SQLException {
  // OracleConnection oConn = conn.unwrap(OracleConnection.class);
  // return oConn.createOracleArray(tvpName, values);
  // }

  @Override
  protected void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne, String tvpName)
      throws SQLException {
      Connection conn = dataBind.getPstmt().getConnection();

      Object[] array = toArray(values, type);
      Array sqlArray = ORACLE_HELP.createArray(conn, tvpName, array);

      dataBind.getPstmt().setArray(dataBind.nextPos(), sqlArray);
  }

  @Override
  protected String getArrayType(int dbType) {
    switch (dbType) {
    case BIT:
    case BOOLEAN:
    case TINYINT:
    case SMALLINT:
    case INTEGER:
    case BIGINT:
    case DECIMAL: // TODO: we have no info about precision here
    case NUMERIC:
      return "EBEAN_NUMBER_TVP";
    case REAL:
    case FLOAT:
    case DOUBLE:
      return "EBEAN_FLOAT_TVP";
    case DATE:
      return "EBEAN_DATE_TVP";
    case TIMESTAMP:
    case TIME_WITH_TIMEZONE:
    case TIMESTAMP_WITH_TIMEZONE:
      return null; // NO: Does not work reliable due time zone issues! - Fall back to normal query
      //return "EBEAN_TIMESTAMP_TVP";
    // case LONGVARCHAR:
    // case CLOB:
    case CHAR:
    case VARCHAR:
      // case LONGNVARCHAR:
      // case NCLOB:
    case NCHAR:
    case NVARCHAR:
      return "EBEAN_STRING_TVP";

    case BINARY:
      return "EBEAN_BINARY_TVP";

    default:
      return null;
    }
  }

  @Override
  protected String getInExpression(boolean not, ScalarType<?> type, int size, String tvpName) {
    if (not) {
      return " not in (SELECT * FROM TABLE (SELECT ? FROM DUAL)) ";
    } else {
      return " in (SELECT * FROM TABLE (SELECT ? FROM DUAL)) ";
    }
  }

}
