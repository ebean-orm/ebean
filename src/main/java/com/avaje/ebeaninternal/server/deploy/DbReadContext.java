package com.avaje.ebeaninternal.server.deploy;

import java.util.Map;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.type.DataReader;

/**
 * Context provided when a BeanProperty reads from a ResultSet.
 */
public interface DbReadContext {

  /**
   * Return the state of the object graph.
   */
  Boolean isReadOnly();

  /**
   * Propagate the state to the bean.
   */
  void propagateState(Object e);

  /**
   * Return the DataReader.
   */
  DataReader getDataReader();

  /**
   * Return true if the query is using supplied SQL rather than generated SQL.
   */
  boolean isRawSql();

  /**
   * Set the JoinNode - used by proxy/reference beans for profiling.
   */
  void setCurrentPrefix(String currentPrefix, Map<String, String> pathMap);

  /**
   * Return true if we are profiling this query.
   */
  boolean isAutoTuneProfiling();

  /**
   * Add AutoTune profiling for a loaded entity bean.
   */
  void profileBean(EntityBeanIntercept ebi, String prefix);

  /**
   * Return the persistence context.
   */
  PersistenceContext getPersistenceContext();

  /**
   * Register a reference for lazy loading.
   */
  void register(String path, EntityBeanIntercept ebi);

  /**
   * Register a collection for lazy loading.
   */
  void register(String path, BeanCollection<?> bc);

  /**
   * Return the property that is associated with the many. There can only be
   * one. This can be null.
   */
  BeanPropertyAssocMany<?> getManyProperty();

  /**
   * Set back the bean that has just been loaded with its id.
   */
  void setLazyLoadedChildBean(EntityBean loadedBean, Object parentId);

  /**
   * Return the query mode.
   */
  SpiQuery.Mode getQueryMode();

  /**
   * Return true if the underlying query is a 'asDraft' query.
   */
  boolean isDraftQuery();

  /**
   * Return true if this request disables lazy loading.
   */
  boolean isDisableLazyLoading();
}
