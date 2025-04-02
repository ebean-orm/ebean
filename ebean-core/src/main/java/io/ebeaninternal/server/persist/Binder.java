package io.ebeaninternal.server.persist;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;
import io.ebeaninternal.server.persist.platform.MultiValueBind;
import io.ebeaninternal.server.type.*;

import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Collection;
import java.util.List;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Binds bean values to a PreparedStatement.
 */
public final class Binder {

  private final TypeManager typeManager;
  private final int asOfBindCount;
  private final boolean asOfStandardsBased;
  private final DbExpressionHandler dbExpressionHandler;
  private final DataTimeZone dataTimeZone;
  private final MultiValueBind multiValueBind;
  private final boolean enableBindLog;
  private final GeoTypeBinder geoTypeBinder;

  public Binder(TypeManager typeManager, SpiLogManager logManager, int asOfBindCount, boolean asOfStandardsBased,
                DbExpressionHandler dbExpressionHandler, DataTimeZone dataTimeZone, MultiValueBind multiValueBind) {
    this.typeManager = typeManager;
    this.geoTypeBinder = typeManager.geoTypeBinder();
    this.asOfBindCount = asOfBindCount;
    this.asOfStandardsBased = asOfStandardsBased;
    this.dbExpressionHandler = dbExpressionHandler;
    this.dataTimeZone = dataTimeZone;
    this.multiValueBind = multiValueBind;
    this.enableBindLog = logManager.enableBindLog();
  }

  /**
   * Return true if bind log is enabled.
   */
  public boolean isEnableBindLog() {
    return enableBindLog;
  }

  /**
   * Return the bind count per predicate for 'As Of' query predicates.
   */
  public int getAsOfBindCount() {
    return asOfBindCount;
  }

  /**
   * Return true if the 'as of' history support is SQL2011 standards based.
   */
  public boolean isAsOfStandardsBased() {
    return asOfStandardsBased;
  }

  /**
   * Bind the parameters to the preparedStatement returning the bind log.
   */
  public String bind(BindParams bindParams, PreparedStatement statement, Connection connection) throws SQLException {
    return bind(bindParams, new DataBind(dataTimeZone, statement, connection));
  }

  /**
   * Bind the list of positionedParameters in BindParams.
   */
  private String bind(BindParams bindParams, DataBind dataBind) throws SQLException {
    StringBuilder bindLog = new StringBuilder();
    bind(bindParams, dataBind, bindLog);
    return bindLog.toString();
  }

  /**
   * Bind the list of positionedParameters in BindParams.
   */
  public void bind(BindParams bindParams, DataBind dataBind, StringBuilder bindLog) throws SQLException {
    bind(bindParams.positionedParameters(), dataBind, bindLog);
  }

  /**
   * Bind the list of parameters..
   */
  private void bind(List<BindParams.Param> list, DataBind dataBind, StringBuilder bindLog) throws SQLException {
    CallableStatement cstmt = null;
    if (dataBind.getPstmt() instanceof CallableStatement) {
      cstmt = (CallableStatement) dataBind.getPstmt();
    }
    // the iterator is assumed to be in the correct order
    Object value = null;
    try {
      for (BindParams.Param param : list) {

        if (param.isOutParam() && cstmt != null) {
          cstmt.registerOutParameter(dataBind.nextPos(), param.type());
          if (param.isInParam()) {
            dataBind.decrementPos();
          }
        }
        if (param.isInParam()) {
          value = param.inValue();
          if (bindLog != null) {
            if (bindLog.length() > 0) {
              bindLog.append(", ");
            }
            if (param.isEncryptionKey()) {
              bindLog.append("****");
            } else {
              String sv = String.valueOf(value);
              if (sv.length() > 50) {
                sv = sv.substring(0, 47) + "...";
              }
              bindLog.append(sv);
            }
          }
          if (value instanceof Collection) {
            for (Object entry: (Collection<?>) value) {
              bindObject(dataBind, entry);
            }
          } else if (value == null) {
            // this doesn't work for query predicates
            bindObject(dataBind, null, param.type());
          } else {
            bindObject(dataBind, value);
          }
        }
      }

    } catch (SQLException ex) {
      CoreLog.log.log(WARNING, "error binding parameter [{0}][{1}]", (dataBind.currentPos() - 1), value);
      throw ex;
    }
  }

  /**
   * Return true if MultiValue binding is supported for the given type.
   */
  public boolean isMultiValueSupported(Class<?> cls) {
    try {
      ScalarType<?> scalarType = getScalarType(cls);
      return multiValueBind.isTypeSupported(scalarType.jdbcType());
    } catch (PersistenceException e) {
      return false;
    }
  }

  public ScalarType<?> getScalarType(Class<?> clazz) {
    ScalarType<?> type = typeManager.type(clazz);
    if (type == null) {
      throw new PersistenceException("No ScalarType registered for " + clazz);
    }
    return type;
  }

  /**
   * Bind an Object with unknown data type.
   */
  public void bindObject(DataBind dataBind, Object value) throws SQLException {
    if (value == null) {
      // null of unknown type
      bindObject(dataBind, null, Types.OTHER);

    } else if (value instanceof MultiValueWrapper) {
      MultiValueWrapper wrapper = (MultiValueWrapper) value;
      Collection<?> values = wrapper.getValues();

      ScalarType<?> type = getScalarType(wrapper.getType());
      int dbType = type.jdbcType();
      // let the multiValueBind decide what to do with the value
      multiValueBind.bindMultiValues(dataBind, values, type, one -> bindObject(dataBind, one, dbType));

    } else {
      ScalarType<?> type = getScalarType(value.getClass());
      if (!type.jdbcNative()) {
        // convert to a JDBC native type
        value = type.toJdbcType(value);
      }
      bindObject(dataBind, value, type.jdbcType());
    }
  }

  /**
   * Return the SQL in clause taking into account Multi-value support.
   */
  public String getInExpression(boolean not, List<Object> bindValues) {
    ScalarType<?> type = getScalarType(bindValues.get(0).getClass());
    return multiValueBind.getInExpression(not, type, bindValues.size());
  }

  /**
   * bind a single value.
   * <p>
   * Note that java.math.BigInteger is supported by converting it to a Long.
   * </p>
   * <p>
   * Note if we get a java.util.Date or java.util.Calendar then these have
   * been anonymously passed in (UpdateSql etc). There is a global setting to
   * convert then to a java.sql.Date or java.sql.Timestamp for binding. The
   * default is that both are converted to java.sql.Timestamp.
   * </p>
   */
  private void bindObject(DataBind dataBind, Object data, int dbType) throws SQLException {
    if (data == null) {
      dataBind.setNull(dbType);
      return;
    }
    switch (dbType) {
      case java.sql.Types.LONGVARCHAR:
        bindLongVarChar(dataBind, data);
        break;

      case java.sql.Types.LONGVARBINARY:
        bindLongVarBinary(dataBind, data);
        break;

      case java.sql.Types.CLOB:
        bindClob(dataBind, data);
        break;

      case java.sql.Types.BLOB:
        bindBlob(dataBind, data);
        break;

      default:
        bindSimpleData(dataBind, dbType, data);
    }
  }

  /**
   * Binds the value to the statement according to the data type.
   */
  private void bindSimpleData(DataBind b, int dataType, Object data) {
    try {
      switch (dataType) {
        case java.sql.Types.BOOLEAN:
        case java.sql.Types.BIT:
          // Types.BIT should map to Java Boolean
          b.setBoolean((Boolean) data);
          break;

        case java.sql.Types.VARCHAR:
          b.setString((String) data);
          break;

        case java.sql.Types.CHAR:
          b.setString(data.toString());
          break;

        case java.sql.Types.TINYINT:
          b.setByte((Byte) data);
          break;

        case java.sql.Types.SMALLINT:
          b.setShort((Short) data);
          break;

        case java.sql.Types.INTEGER:
          b.setInt((Integer) data);
          break;

        case java.sql.Types.BIGINT:
          b.setLong((Long) data);
          break;

        case java.sql.Types.REAL:
          b.setFloat((Float) data);
          break;

        case java.sql.Types.FLOAT:
        case java.sql.Types.DOUBLE:
          // DB Float in theory maps to Java Double type
          b.setDouble((Double) data);
          break;

        case java.sql.Types.NUMERIC:
        case java.sql.Types.DECIMAL:
          b.setBigDecimal((BigDecimal) data);
          break;

        case java.sql.Types.TIME:
          b.setTime((java.sql.Time) data);
          break;

        case java.sql.Types.DATE:
          b.setDate((java.sql.Date) data);
          break;

        case java.sql.Types.TIMESTAMP:
        case DbPlatformType.LOCALDATETIME:
          b.setTimestamp((java.sql.Timestamp) data);
          break;

        case java.sql.Types.BINARY:
        case java.sql.Types.VARBINARY:
          b.setBytes((byte[]) data);
          break;

        case DbPlatformType.UUID:
        case java.sql.Types.JAVA_OBJECT:
          // Not too sure about this.
          // native UUID support in H2 and Postgres
          b.setObject(data);
          break;

        case DbPlatformType.INET:
          // data is always a String at this point
          b.setObject(PostgresHelper.asInet(data.toString()));
          break;

        case DbPlatformType.POINT:
        case DbPlatformType.POLYGON:
        case DbPlatformType.MULTIPOINT:
        case DbPlatformType.LINESTRING:
        case DbPlatformType.MULTILINESTRING:
        case DbPlatformType.MULTIPOLYGON:
          geoTypeBinder.bind(b, dataType, data);
          break;

        case java.sql.Types.OTHER:
          b.setObject(data, dataType);
          break;

        default:
          throw new SQLException("Unhandled data type:" + dataType + " bind number:" + b.currentPos());
      }

    } catch (Exception e) {
      String dataClass = "Data is null?";
      if (data != null) {
        dataClass = data.getClass().getName();
      }
      throw new PersistenceException("Error with property:" + b.currentPos() + " dt:" + dataType + " data:" + data + " " + dataClass, e);
    }
  }

  /**
   * Bind String data to a LONGVARCHAR column.
   */
  private void bindLongVarChar(DataBind dataBind, Object data) throws SQLException {
    dataBind.setClob((String) data);
  }

  /**
   * Bind byte[] data to a LONGVARBINARY column.
   */
  private void bindLongVarBinary(DataBind dataBind, Object data) throws SQLException {
    dataBind.setBlob((byte[]) data);
  }

  /**
   * Bind String data to a CLOB column.
   */
  private void bindClob(DataBind dataBind, Object data) throws SQLException {
    dataBind.setClob((String) data);
  }

  /**
   * Bind byte[] data to a BLOB column.
   */
  private void bindBlob(DataBind dataBind, Object data) throws SQLException {
    dataBind.setBlob((byte[]) data);
  }

  private boolean isLob(int dbType) {
    switch (dbType) {
      case Types.CLOB:
      case Types.LONGVARBINARY:
      case Types.BLOB:
      case Types.LONGVARCHAR:
        return true;

      default:
        return false;
    }
  }

  /**
   * Return the DB platform specific expression handler (for JSON and ARRAY types).
   */
  public DbExpressionHandler getDbExpressionHandler() {
    return dbExpressionHandler;
  }

  /**
   * Create and return a DataBind for the statement.
   */
  public DataBind dataBind(PreparedStatement stmt, Connection connection) {
    return new DataBind(dataTimeZone, stmt, connection);
  }

  public DataReader createDataReader(boolean unmodifiable, ResultSet resultSet) {
    return new RsetDataReader(unmodifiable, dataTimeZone, resultSet);
  }
}
