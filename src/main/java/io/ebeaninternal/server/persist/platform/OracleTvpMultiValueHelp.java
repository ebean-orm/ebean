package io.ebeaninternal.server.persist.platform;

import static java.sql.Types.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;

// import oracle.jdbc.*

/**
 * Multi value binder that uses oracle's createOracleArray.
 * 
 * Unfortunately, oracle did not implement createArray and if we import oracle.jdbc,
 * we need the driver to compile ebean. The driver is not freely available,
 * that's why we have to use some reflection to access this method.
 * 
 * It relies that the tvp-types are created by DDL.
 * 
 * @author Roland Praml, FOCONIS AG
 *
 */


public class OracleTvpMultiValueHelp extends MultiValueHelp {
  
  
  private static final int MIN_LENGTH = 16; 

  // need to use some reflection, otherwise we will need oracle driver for compiling :(
  private static final Class<? extends Connection> ORACLE_CONNECTION = getConnectionClass();
  
  private static final Method CREATE_ORACLE_ARRAY = getCreateOracleArrayMethod(ORACLE_CONNECTION);
  
  @SuppressWarnings("unchecked")
  private static Class<? extends Connection> getConnectionClass() {
    try {
      return (Class<? extends Connection>) Class.forName("oracle.jdbc.OracleConnection");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static Method getCreateOracleArrayMethod(Class<? extends Connection> cls) {
      try {
        return cls.getMethod("createOracleArray", String.class, Object.class);
      } catch (NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }
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
  // -------------


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
  public String getInExpression(Binder binder,  boolean not, Object[] values) {
   
    if (values.length < MIN_LENGTH) {
      return super.getInExpression(binder, not, values);
    } else {
      int dbType = binder.getScalarType(values[0].getClass()).getJdbcType();
      String tvpName = getTvpName(dbType);
      if (tvpName == null || values.length < MIN_LENGTH) {
        return super.getInExpression(binder, not, values);
      } else {
        if (not) {
          return " not in (select * from table (select ? from dual)) ";
        } else {
          return " in (select * from table (select ? from dual)) ";
        }
      }
    }
  }

}
