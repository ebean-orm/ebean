package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestUpdateSql;

/**
 * The actual execution of persist requests.
 * <p>
 * A Persister 'front-ends' this object and handles the
 * batching, cascading, concurrency mode detection etc.
 * </p>
 *
 */
public interface PersistExecute {
	
	/**
	 * Create a BatchControl for the current transaction.
	 */
	public BatchControl createBatchControl(SpiTransaction t);
	
	/**
	 * Execute a Bean (or MapBean) insert.
	 */
	public <T> void executeInsertBean(PersistRequestBean<T> request);

	/**
	 * Execute a Bean (or MapBean) update.
	 */
	public <T> void executeUpdateBean(PersistRequestBean<T> request);

	/**
	 * Execute a Bean (or MapBean) delete.
	 */
	public <T> void executeDeleteBean(PersistRequestBean<T> request);

	/**
	 * Execute a Update.
	 */
	public int executeOrmUpdate(PersistRequestOrmUpdate request);
	
	/**
	 * Execute a CallableSql.
	 */
	public int executeSqlCallable(PersistRequestCallableSql request);

	/**
	 * Execute a UpdateSql.
	 */
	public int executeSqlUpdate(PersistRequestUpdateSql request);

}
