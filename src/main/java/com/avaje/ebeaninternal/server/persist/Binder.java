package com.avaje.ebeaninternal.server.persist;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.server.core.DbExpressionHandler;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.timezone.DataTimeZone;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binds bean values to a PreparedStatement.
 */
public class Binder {

  private static final Logger logger = LoggerFactory.getLogger(Binder.class);

  private final TypeManager typeManager;

  private final int asOfBindCount;

  private final boolean bindAsOfWithFromClause;

  private final DbExpressionHandler dbExpressionHandler;

  private final DataTimeZone dataTimeZone;

  /**
   * Set the PreparedStatement with which to bind variables to.
   */
  public Binder(TypeManager typeManager, int asOfBindCount, boolean bindAsOfWithFromClause,
                DbExpressionHandler dbExpressionHandler, DataTimeZone dataTimeZone) {

    this.typeManager = typeManager;
    this.asOfBindCount = asOfBindCount;
    this.bindAsOfWithFromClause = bindAsOfWithFromClause;
    this.dbExpressionHandler = dbExpressionHandler;
    this.dataTimeZone = dataTimeZone;
  }

  /**
   * Return the bind count per predicate for 'As Of' query predicates.
   */
  public int getAsOfBindCount() {
    return asOfBindCount;
  }

  /**
   * Return true if the 'as of' predicates are in the from/join clause in which case the timestamp is
   * bound early (before all the other predicates ala Oracle). Return false if the 'as of' predicates are
   * appended to the end of the predicates and the timestamp is bound last (Postgres, MySql).
   */
  public boolean isBindAsOfWithFromClause() {
    return bindAsOfWithFromClause;
  }

  /**
   * Bind the values to the Prepared Statement.
   */
  public void bind(BindValues bindValues, DataBind dataBind, StringBuilder bindBuf) throws SQLException {

    String logPrefix = "";

    ArrayList<BindValues.Value> list = bindValues.values();
    for (int i = 0; i < list.size(); i++) {
      BindValues.Value bindValue = list.get(i);

      Object val = bindValue.getValue();
      int dt = bindValue.getDbType();
      bindObject(dataBind, val, dt);

      if (bindBuf != null) {
        bindBuf.append(logPrefix);
        if (logPrefix.equals("")) {
          logPrefix = ", ";
        }
        bindBuf.append(bindValue.getName());
        bindBuf.append("=");
        if (isLob(dt)) {
          bindBuf.append("[LOB]");
        } else {
          bindBuf.append(String.valueOf(val));
        }
      }
    }
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
  public String bind(BindParams bindParams, DataBind dataBind) throws SQLException {

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
  public void bind(List<BindParams.Param> list, DataBind dataBind, StringBuilder bindLog) throws SQLException {

    CallableStatement cstmt = null;

    if (dataBind.getPstmt() instanceof CallableStatement) {
      cstmt = (CallableStatement) dataBind.getPstmt();
    }

    // the iterator is assumed to be in the correct order
    Object value = null;
    try {
      for (int i = 0; i < list.size(); i++) {

        BindParams.Param param = list.get(i);

        if (param.isOutParam() && cstmt != null) {
          cstmt.registerOutParameter(dataBind.nextPos(), param.getType());
          if (param.isInParam()) {
            dataBind.decrementPos();
          }
        }
        if (param.isInParam()) {
          value = param.getInValue();
          if (bindLog != null) {
            if (param.isEncryptionKey()) {
              bindLog.append("****");
            } else {
              bindLog.append(value);
            }
            bindLog.append(", ");
          }
          if (value == null) {
            // this doesn't work for query predicates
            bindObject(dataBind, null, param.getType());
          } else {
            bindObject(dataBind, value);
          }
        }
      }

    } catch (SQLException ex) {
      logger.warn(Message.msg("fetch.bind.error", "" + (dataBind.currentPos() - 1), value));
      throw ex;
    }
  }

  /**
   * Bind an Object with unknown data type.
   */
  public Object bindObject(DataBind dataBind, Object value) throws SQLException {

    if (value == null) {
      // null of unknown type
      bindObject(dataBind, null, Types.OTHER);
      return null;

    } else {

      ScalarType<?> type = typeManager.getScalarType(value.getClass());
      if (type == null) {
        // the type is not registered with the TypeManager.
        String msg = "No ScalarType registered for " + value.getClass();
        throw new PersistenceException(msg);

      } else if (!type.isJdbcNative()) {
        // convert to a JDBC native type
        value = type.toJdbcType(value);
      }

      int dbType = type.getJdbcType();
      bindObject(dataBind, value, dbType);
      return value;
    }
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
  public void bindObject(DataBind dataBind, Object data, int dbType) throws SQLException {

    if (data == null) {
      dataBind.setNull(dbType);
      return;
    }

    switch (dbType) {
      case Types.LONGVARCHAR:
        bindLongVarChar(dataBind, data);
        break;

      case Types.LONGVARBINARY:
        bindLongVarBinary(dataBind, data);
        break;

      case Types.CLOB:
        bindClob(dataBind, data);
        break;

      case Types.BLOB:
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
        case Types.BOOLEAN:
          b.setBoolean((Boolean) data);
          break;
        case Types.BIT:
          // Types.BIT should map to Java Boolean
          b.setBoolean((Boolean) data);
          break;

        case Types.VARCHAR:
          b.setString((String) data);
          break;

        case Types.CHAR:
          b.setString(data.toString());
          break;

        case Types.TINYINT:
          b.setByte((Byte) data);
          break;

        case Types.SMALLINT:
          b.setShort((Short) data);
          break;

        case Types.INTEGER:
          b.setInt((Integer) data);
          break;

        case Types.BIGINT:
          b.setLong((Long) data);
          break;

        case Types.REAL:
          b.setFloat((Float) data);
          break;

        case Types.FLOAT:
          // DB Float in theory maps to Java Double type
          b.setDouble((Double) data);
          break;

        case Types.DOUBLE:
          b.setDouble((Double) data);
          break;

        case Types.NUMERIC:
          b.setBigDecimal((BigDecimal) data);
          break;

        case Types.DECIMAL:
          b.setBigDecimal((BigDecimal) data);
          break;

        case Types.TIME:
          b.setTime((Time) data);
          break;

        case Types.DATE:
          b.setDate((Date) data);
          break;

        case Types.TIMESTAMP:
          b.setTimestamp((Timestamp) data);
          break;

        case Types.BINARY:
          b.setBytes((byte[]) data);
          break;

        case Types.VARBINARY:
          b.setBytes((byte[]) data);
          break;

        case DbType.UUID:
          // native UUID support in H2 and Postgres
          b.setObject(data);
          break;

        case Types.OTHER:
          b.setObject(data);
          break;

        case Types.JAVA_OBJECT:
          // Not too sure about this.
          b.setObject(data);
          break;

        default:
          String msg = Message.msg("persist.bind.datatype", "" + dataType, "" + b.currentPos());
          throw new SQLException(msg);
      }

    } catch (Exception e) {
      String dataClass = "Data is null?";
      if (data != null) {
        dataClass = data.getClass().getName();
      }
      String m = "Error with property[" + b.currentPos() + "] dt[" + dataType + "]";
      m += "data[" + data + "][" + dataClass + "]";
      throw new PersistenceException(m, e);
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
        return true;
      case Types.LONGVARCHAR:
        return true;
      case Types.BLOB:
        return true;
      case Types.LONGVARBINARY:
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
}
