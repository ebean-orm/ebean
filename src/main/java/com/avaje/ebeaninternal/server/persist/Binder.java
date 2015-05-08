package com.avaje.ebeaninternal.server.persist;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.server.core.Message;
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

	//private final Calendar calendar;

	private final TypeManager typeManager;

	/**
	 * Set the PreparedStatement with which to bind variables to.
	 */
	public Binder(TypeManager typeManager) {

		this.typeManager = typeManager;
		//this.calendar = new GregorianCalendar();
	}

	/**
	 * Bind the values to the Prepared Statement.
	 */
	public void bind(BindValues bindValues, DataBind dataBind, StringBuilder bindBuf)
			throws SQLException {

		String logPrefix = "";

		ArrayList<BindValues.Value> list = bindValues.values();
		for (int i = 0; i < list.size(); i++) {
			BindValues.Value bindValue = list.get(i);
			if (bindValue.isComment()) {
				if (bindBuf != null) {
					bindBuf.append(bindValue.getName());
					if (logPrefix.equals("")) {
						logPrefix = ", ";
					}
				}
			} else {
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
	}

	/**
	 * Bind the list of positionedParameters in BindParams.
	 */
	public String bind(BindParams bindParams, DataBind dataBind)
		throws SQLException {

		StringBuilder bindLog = new StringBuilder();
		bind(bindParams, dataBind, bindLog);
		return bindLog.toString();
	}

	/**
	 * Bind the list of positionedParameters in BindParams.
	 */
	public void bind(BindParams bindParams, DataBind dataBind, StringBuilder bindLog)
		throws SQLException {

		bind(bindParams.positionedParameters(), dataBind, bindLog);
	}
	
	/**
	 * Bind the list of parameters..
	 */
	public void bind(List<BindParams.Param> list, DataBind dataBind, StringBuilder bindLog)
			throws SQLException {

		CallableStatement cstmt = null;

		if (dataBind.getPstmt() instanceof CallableStatement) {
			cstmt = (CallableStatement) dataBind.getPstmt();
		}

		// the iterator is assumed to be in the correct order
		Object value = null;
		try {
			for (int i = 0; i < list.size(); i++) {

				BindParams.Param param = list.get(i);

				if (param.isOutParam() && cstmt != null){
					cstmt.registerOutParameter(dataBind.nextPos(), param.getType());
					if (param.isInParam()) {
					    dataBind.decrementPos();
					}
				}
				if (param.isInParam()) {
					value = param.getInValue();
					if (bindLog != null) {
					    if (param.isEncryptionKey()){
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
	public void bindObject(DataBind dataBind, Object value) throws SQLException {

		if (value == null) {
			// null of unknown type
			bindObject(dataBind, null, Types.OTHER);

		} else {

			ScalarType<?> type = typeManager.getScalarType(value.getClass());
			if (type == null){
				// the type is not registered with the TypeManager.
				String msg = "No ScalarType registered for "+value.getClass();
				throw new PersistenceException(msg);
				
			} else if (!type.isJdbcNative()) {
				// convert to a JDBC native type
				value = type.toJdbcType(value);
			}

			int dbType = type.getJdbcType();
			bindObject(dataBind, value, dbType);
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
	public void bindObject(DataBind dataBind, Object data, int dbType)
			throws SQLException {

		if (data == null){
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
	private void bindSimpleData(DataBind b, int dataType, Object data)
			throws SQLException {

		try {
			switch (dataType) {
			case java.sql.Types.BOOLEAN:
				b.setBoolean((Boolean) data);
				break;
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
				// DB Float in theory maps to Java Double type
				b.setDouble((Double) data);
				break;

			case java.sql.Types.DOUBLE:
				b.setDouble((Double) data);
				break;

			case java.sql.Types.NUMERIC:
				b.setBigDecimal((BigDecimal) data);
				break;

			case java.sql.Types.DECIMAL:
				b.setBigDecimal((BigDecimal) data);
				break;

			case java.sql.Types.TIME:
				//pstmt.setTime(index, (java.sql.Time) data, calendar);
				b.setTime((java.sql.Time) data);
				break;

			case java.sql.Types.DATE:
				//pstmt.setDate(index, (java.sql.Date) data, calendar);
				b.setDate((java.sql.Date) data);
				break;

			case java.sql.Types.TIMESTAMP:
				//pstmt.setTimestamp(index, (java.sql.Timestamp) data, calendar);
				b.setTimestamp((java.sql.Timestamp) data);
				break;

			case java.sql.Types.BINARY:
				b.setBytes((byte[]) data);
				break;

			case java.sql.Types.VARBINARY:
				b.setBytes((byte[]) data);
				break;

			case java.sql.Types.OTHER:
				b.setObject(data);
				break;

			case java.sql.Types.JAVA_OBJECT:
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
	private void bindLongVarChar(DataBind b, Object data)
			throws SQLException {

		String sd = (String) data;
		b.setClob(sd);
	}

	/**
	 * Bind byte[] data to a LONGVARBINARY column.
	 */
	private void bindLongVarBinary(DataBind b, Object data)
			throws SQLException {

		byte[] bytes = (byte[]) data;
		b.setBlob(bytes);
	}

	/**
	 * Bind String data to a CLOB column.
	 */
	private void bindClob(DataBind b, Object data) throws SQLException {

		String sd = (String) data;
		b.setClob(sd);
	}

	/**
	 * Bind byte[] data to a BLOB column.
	 */
	private void bindBlob(DataBind b, Object data) throws SQLException {

		byte[] bytes = (byte[]) data;
		b.setBlob(bytes);
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
}
