package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;
/**
 * Multi value binder that uses oracle's createOracleArray.
 * 
 * Unfortunately, oracle did not implement createArray, and the driver is not
 * freely available, that's why we have to use some reflection to access this method.
 * 
 * It relies that the tvp-types are created by DDL.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class OracleTvpMultiValueHelp extends MultiValueHelp {
  
  
  private static final int MIN_LENGTH = 16; 

  // need to use some reflection, otherwise we will need oracle driver for compiling :(
  private static final Class<? extends Connection> ORACLE_CONNECTION;
  private static final Method CREATE_ORACLE_ARRAY;
  static {
    Class<?> cls = null;
    Method method = null;
    try {
      cls = Class.forName("oracle.jdbc.OracleConnection");
      method = cls.getMethod("createOracleArray", String.class, Object.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    ORACLE_CONNECTION = (Class<? extends Connection>) cls;
    CREATE_ORACLE_ARRAY = method;
  }

  private Array createArray(Connection conn, String tvpName, Object[] values) throws SQLException {
    Connection oConn = conn.unwrap(ORACLE_CONNECTION);
    try {
      return (Array) CREATE_ORACLE_ARRAY.invoke(oConn, tvpName, values);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof SQLException) {
        throw (SQLException) e.getTargetException();
      } else {
        throw new SQLException(e);
      }
    } catch (IllegalAccessException | IllegalArgumentException e) {
      throw new SQLException(e);
    }
  }
  
  // code without reflection
  //  private Array createArray(Connection conn, String tvpName, Object[] values) throws SQLException {
  //    OracleConnection oConn = conn.unwrap(OracleConnection.class);
  //    return oConn.createOracleArray(tvpName, values);
  //  }
  
  @Override
  public void bindMultiValues(Binder binder, DataBind dataBind, Object[] values, int dbType) throws SQLException {
    String tvpName = getTvpName(dbType);
    if (tvpName == null || values.length < MIN_LENGTH) {
      super.bindMultiValues(binder, dataBind, values, dbType);
    } else {
      Connection conn = dataBind.getPstmt().getConnection();
      
      Array arrayToPass = createArray(conn, tvpName, values);

      dataBind.getPstmt().setArray(dataBind.nextPos(), arrayToPass);
    }
  }







  private String getTvpName(int dbType) {
    switch(dbType) {
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
        return "EBEAN_TIMESTAMP_TVP";
      //case LONGVARCHAR:
      //case CLOB:
      case CHAR:
      case VARCHAR:
        //case LONGNVARCHAR:
      //case NCLOB:
      case NCHAR:
      case NVARCHAR:
        return "EBEAN_STRING_TVP";
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
        request.append(propName).append(" not in (select * from table (select ? from dual)) ");
      } else {
        request.append(propName).append(" in (select * from table (select ? from dual)) ");
      }
    }
  }

}
