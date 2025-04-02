package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.api.SpiQuery;

import java.util.Map;

/**
 * Context provided when a BeanProperty reads from a ResultSet.
 */
public interface DbReadContext {

  /**
   * Return the DataReader.
   */
  DataReader dataReader();

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
  PersistenceContext persistenceContext();

  /**
   * Register a reference for lazy loading.
   */
  void register(String path, EntityBeanIntercept ebi);

  /**
   * Register a reference with inheritance for lazy loading.
   */
  void registerBeanInherit(BeanPropertyAssocOne<?> property, EntityBeanIntercept ebi);

  /**
   * Register a collection for lazy loading.
   */
  void register(BeanPropertyAssocMany<?> many, BeanCollection<?> bc);

  /**
   * Set back the bean that has just been loaded with its id.
   */
  void setLazyLoadedChildBean(EntityBean loadedBean, Object parentId);

  /**
   * Return the query mode.
   */
  SpiQuery.Mode queryMode();

  /**
   * Return true if this request disables lazy loading.
   */
  boolean isDisableLazyLoading();

  /**
   * Return true if the beans that already exist in the persistence context
   * should have data from the database loaded into them.
   * <p>
   * This is the case for REFRESH and forUpdate queries.
   */
  boolean isLoadContextBean();

  /**
   * Handles a load error on given property.
   */
  void handleLoadError(String fullName, Exception e);

  /**
   * Return true if this many property should be included in unmodifiable
   * query via a secondary query.
   */
  boolean includeSecondary(BeanPropertyAssocMany<?> many);

  /**
   * Return true if we are loading unmodifiable beans.
   */
  boolean unmodifiable();
}
