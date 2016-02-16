package com.avaje.ebeaninternal.api;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.List;

/**
 * Controls the loading of reference objects for a query instance.
 */
public interface LoadContext {

  /**
   * Return the minimum batch size when using QueryIterator with query joins.
   */
  int getSecondaryQueriesMinBatchSize(int defaultQueryBatch);

	/**
	 * Execute any secondary (+query) queries if there are any defined.
	 * @param parentRequest the originating query request
	 */
	void executeSecondaryQueries(OrmQueryRequest<?> parentRequest);

	/**
	 * Register any secondary queries (+query or +lazy) with their appropriate LoadBeanContext or LoadManyContext.
	 */
  void registerSecondaryQueries(List<OrmQueryProperties> queryJoins, List<OrmQueryProperties> lazyJoins);

  /**
	 * Return the node for a given path which is used by AutoTune profiling.
	 */
	ObjectGraphNode getObjectGraphNode(String path);

	/**
	 * Return the persistence context used by this query and future lazy loading.
	 */
	PersistenceContext getPersistenceContext();

	/**
	 * Set the persistence context used by this query and future lazy loading.
	 * <p>
	 * Used by query iterator when processing large result sets.
	 * </p>
	 */
	void resetPersistenceContext(PersistenceContext persistenceContext);

	/**
	 * Register a Bean for lazy loading.
	 */
	void register(String path, EntityBeanIntercept ebi);

	/**
	 * Register a collection for lazy loading.
	 */
	void register(String path, BeanCollection<?> bc);

}
