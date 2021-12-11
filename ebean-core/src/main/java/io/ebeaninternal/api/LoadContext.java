package io.ebeaninternal.api;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Controls the loading of reference objects for a query instance.
 */
public interface LoadContext {

  /**
   * Return the minimum batch size when using QueryIterator with query joins.
   */
  int getSecondaryQueriesMinBatchSize();

  /**
   * Execute any secondary (+query) queries if there are any defined.
   *
   * @param parentRequest the originating query request
   * @param forEach       set true when using findEach iteration
   */
  void executeSecondaryQueries(OrmQueryRequest<?> parentRequest, boolean forEach);

  /**
   * Return the node for a given path which is used by AutoTune profiling.
   */
  ObjectGraphNode getObjectGraphNode(String path);

  /**
   * Return the persistence context used by this query and future lazy loading.
   */
  PersistenceContext getPersistenceContext();

  /**
   * Register a Bean for lazy loading.
   */
  void register(String path, EntityBeanIntercept ebi);

  /**
   * Register a Bean with inheritance.
   */
  void register(String path, EntityBeanIntercept ebi, BeanPropertyAssocOne<?> property);

  /**
   * Register a collection for lazy loading.
   */
  void register(String path, BeanPropertyAssocMany<?> many, BeanCollection<?> bc);
}
