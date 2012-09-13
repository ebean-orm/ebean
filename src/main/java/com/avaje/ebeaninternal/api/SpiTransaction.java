/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.api;

import java.sql.Connection;
import java.util.List;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer;

/**
 * Extends Transaction with additional API required on server.
 * <p>
 * Provides support for batching and TransactionContext.
 * </p>
 */
public interface SpiTransaction extends Transaction {

	/**
	 * Return true if generated SQL and Bind values should be logged to the
	 * transaction log.
	 */
	public boolean isLogSql();

	/**
	 * Return true if summary level events should be logged to the transaction
	 * log.
	 */
	public boolean isLogSummary();

	/**
	 * Log a comment to the transaction log for Ebean INTERNAL use. There should
	 * always be an external LogLevel check prior to calling this method.
	 */
	public void logInternal(String msg);

	/**
	 * Return the buffer containing transaction log messages.
	 */
	public TransactionLogBuffer getLogBuffer();

	/**
	 * Register a "Derived Relationship" (that requires an additional update).
	 */
	public void registerDerivedRelationship(DerivedRelationshipData assocBean);

	/**
	 * Return the list of "Derived Relationships" that must be maintained after
	 * insert.
	 */
	public List<DerivedRelationshipData> getDerivedRelationship(Object bean);

	/**
	 * Add a deleting bean to the registered list.
	 * <p>
	 * This is to handle bi-directional relationships where both sides Cascade.
	 * </p>
	 */
	public void registerDeleteBean(Integer hash);

	/**
	 * Unregister the hash of the bean.
	 */
	public void unregisterDeleteBean(Integer hash);

	/**
	 * Return true if this is a bean that has already been saved/deleted.
	 */
	public boolean isRegisteredDeleteBean(Integer hash);

	/**
	 * Unregister the persisted bean.
	 */
	public void unregisterBean(Object bean);

	/**
	 * Return true if this is a bean that has already been persisted in the
	 * current recursive save request. The goal is to stop recursively saving
	 * the bean when cascade persist is on both sides of a relationship).
	 * <p>
	 * This will register the bean if it is not already.
	 * </p>
	 */
	public boolean isRegisteredBean(Object bean);

	/**
	 * Returns a String used to identify the transaction. This id is used for
	 * Transaction logging.
	 */
	public String getId();

	/**
	 * Return the batchSize specifically set for this transaction or 0.
	 * <p>
	 * Returning 0 implies to use the system wide default batch size.
	 * </p>
	 */
	public int getBatchSize();

	/**
	 * Modify and return the current 'depth' of the transaction.
	 * <p>
	 * As we cascade save or delete we traverse the object graph tree. Going up
	 * to Assoc Ones the depth decreases and going down to Assoc Manys the depth
	 * increases.
	 * </p>
	 * <p>
	 * The depth is used for ordering batching statements. The lowest depth get
	 * executed first during save.
	 * </p>
	 */
	public int depth(int diff);

	/**
	 * Return true if this transaction was created explicitly via
	 * <code>Ebean.beginTransaction()</code>.
	 */
	public boolean isExplicit();

	/**
	 * Get the object that holds the event details.
	 * <p>
	 * This information is used maintain the table state, cache and text
	 * indexes. On commit the Table modifications this generates is broadcast
	 * around the cluster (if you have a cluster).
	 * </p>
	 */
	public TransactionEvent getEvent();

	/**
	 * Whether persistCascade is on for save and delete.
	 */
	public boolean isPersistCascade();

	/**
	 * Return true if this request should be batched. Conversely returns false
	 * if this request should be executed immediately.
	 */
	public boolean isBatchThisRequest();

	/**
	 * Return the queue used to batch up persist requests.
	 */
	public BatchControl getBatchControl();

	/**
	 * Set the queue used to batch up persist requests. There should only be one
	 * PersistQueue set per transaction.
	 */
	public void setBatchControl(BatchControl control);

	/**
	 * Return the persistence context associated with this transaction.
	 * <p>
	 * You may wish to hold onto this and set it against another transaction
	 * later. This is along the lines of 'extended persistence context'
	 * behaviour.
	 * </p>
	 */
	public PersistenceContext getPersistenceContext();

	/**
	 * Set the persistence context to this transaction.
	 * <p>
	 * This could be considered similar to 'EJB3 Extended Persistence Context'.
	 * In that you can get the PersistenceContext from a transaction, hold onto
	 * it, and then set it back later to a second transaction. In general there
	 * is one PersistenceContext per Transaction. The getPersistenceContext()
	 * and setPersistenceContext() enable a developer to reuse a single
	 * PersistenceContext with multiple transactions.
	 * </p>
	 */
	public void setPersistenceContext(PersistenceContext context);

	/**
	 * Return the underlying Connection for internal use.
	 * <p>
	 * If the connection is made public from Transaction and the user code calls
	 * that method we can no longer trust the query only status of a
	 * Transaction.
	 * </p>
	 */
	public Connection getInternalConnection();
}
