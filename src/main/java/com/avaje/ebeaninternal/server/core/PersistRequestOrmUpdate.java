package com.avaje.ebeaninternal.server.core;

import java.sql.SQLException;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdate;
import com.avaje.ebeaninternal.api.SpiUpdate.OrmUpdateType;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanManager;
import com.avaje.ebeaninternal.server.persist.PersistExecute;

/**
 * Persist request specifically for CallableSql.
 */
public final class PersistRequestOrmUpdate extends PersistRequest {

	private final BeanDescriptor<?> beanDescriptor;
	
	private SpiUpdate<?> ormUpdate;

	private int rowCount;

	private String bindLog;

	/**
	 * Create.
	 */
	public PersistRequestOrmUpdate(SpiEbeanServer server, BeanManager<?> mgr, SpiUpdate<?> ormUpdate, 
			SpiTransaction t, PersistExecute persistExecute) {
		
		super(server, t, persistExecute);
		this.beanDescriptor = mgr.getBeanDescriptor();
		this.ormUpdate = ormUpdate;
	}
	
	public BeanDescriptor<?> getBeanDescriptor() {
		return beanDescriptor;
	}
	
	@Override
	public int executeNow() {
		return persistExecute.executeOrmUpdate(this);
	}

	@Override
	public int executeOrQueue() {
		return executeStatement();
	}


	/**
	 * Return the UpdateSql.
	 */
	public SpiUpdate<?> getOrmUpdate() {
		return ormUpdate;
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
	 * Set the bound values.
	 */
	public void setBindLog(String bindLog) {
		this.bindLog = bindLog;
	}

	/**
	 * Perform post execute processing.
	 */
	public void postExecute() throws SQLException {

		OrmUpdateType ormUpdateType = ormUpdate.getOrmUpdateType();
		String tableName = ormUpdate.getBaseTable();
		
		if (transaction.isLogSummary()) {
			String m = ormUpdateType + " table[" + tableName + "] rows["+ rowCount + "] bind[" + bindLog + "]";
			transaction.logSummary(m);
		}
		
		if (ormUpdate.isNotifyCache()) {
						
			// add the modification info to the TransactionEvent
			// this is used to invalidate cached objects etc
			switch (ormUpdateType) {
			case INSERT:
				transaction.getEvent().add(tableName, true, false, false);
				break;
			case UPDATE:
				transaction.getEvent().add(tableName, false, true, false);
				break;
			case DELETE:
				transaction.getEvent().add(tableName, false, false, true);
				break;
			default:
				break;
			}
		}
	}

}
