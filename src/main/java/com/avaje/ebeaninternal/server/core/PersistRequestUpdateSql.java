package com.avaje.ebeaninternal.server.core;

import java.sql.SQLException;

import com.avaje.ebean.SqlUpdate;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiSqlUpdate;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.persist.PersistExecute;

/**
 * Persist request specifically for CallableSql.
 */
public final class PersistRequestUpdateSql extends PersistRequest {

	public enum SqlType {
		SQL_UPDATE, SQL_DELETE, SQL_INSERT, SQL_UNKNOWN
	};

	private final SpiSqlUpdate updateSql;

	private int rowCount;

	private String bindLog;

	private SqlType sqlType;

	private String tableName;

	private String description;

	/**
	 * Create.
	 */
	public PersistRequestUpdateSql(SpiEbeanServer server, SqlUpdate updateSql,
			SpiTransaction t, PersistExecute persistExecute) {
		super(server, t, persistExecute);
		this.type = Type.UPDATESQL;
		this.updateSql = (SpiSqlUpdate)updateSql;
	}

	@Override
	public int executeNow() {
		return persistExecute.executeSqlUpdate(this);
	}

	@Override
	public int executeOrQueue() {
		return executeStatement();
	}

	/**
	 * Return the UpdateSql.
	 */
	public SpiSqlUpdate getUpdateSql() {
		return updateSql;
	}

	/**
	 * No concurrency checking so just note the rowCount.
	 */
	public void checkRowCount(int count) throws SQLException {
		this.rowCount = count;
	}

	/**
	 * Always false.
	 */
	public boolean useGeneratedKeys() {
		return false;
	}

	/**
	 * Not called for this type of request.
	 */
	public void setGeneratedKey(Object idValue) {
	}

	/**
	 * Specify the type of statement executed. Used to automatically register
	 * with the transaction event.
	 */
	public void setType(SqlType sqlType, String tableName, String description) {
		this.sqlType = sqlType;
		this.tableName = tableName;
		this.description = description;
	}

	/**
	 * Set the bound values.
	 */
	public void setBindLog(String bindLog) {
		this.bindLog = bindLog;
	}

	/**
	 * Perform post execute processing.
	 */
	public void postExecute() throws SQLException {

		if (transaction.isLogSummary()) {
			String m = description + " table[" + tableName + "] rows["+ rowCount + "] bind[" + bindLog + "]";
			transaction.logSummary(m);
		}

		if (updateSql.isAutoTableMod()) {
			// add the modification info to the TransactionEvent
			// this is used to invalidate cached objects etc
			switch (sqlType) {
			case SQL_INSERT:
				transaction.getEvent().add(tableName, true, false, false);
				break;
			case SQL_UPDATE:
				transaction.getEvent().add(tableName, false, true, false);
				break;
			case SQL_DELETE:
				transaction.getEvent().add(tableName, false, false, true);
				break;
								
			default:
				break;
			}
		}
	}

}
