package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.util.Map;

/**
 * Bean type interface for Sql query tree.
 */
public interface STreeType {

  /**
   * Return the bean short name.
   */
  String name();

  /**
   * Return true if the underlying type has an Id property.
   */
  boolean hasId();

  /**
   * Return true if the type uses soft delete.
   */
  boolean isSoftDelete();

  /**
   * Return true if the type uses history.
   */
  boolean isHistorySupport();

  /**
   * Return true if the type is RawSql based.
   */
  boolean isRawSqlBased();

  /**
   * Return the soft delete predicate using the given table alias.
   */
  String softDeletePredicate(String baseTableAlias);

  /**
   * Return the scalar properties.
   */
  STreeProperty[] propsBaseScalar();

  /**
   * Return the embedded bean properties.
   */
  STreePropertyAssoc[] propsEmbedded();

  /**
   * Return the associated one properties.
   */
  STreePropertyAssocOne[] propsOne();

  /**
   * Return the associated many properties.
   */
  STreePropertyAssocMany[] propsMany();

  /**
   * Return the IdBinder for this type.
   */
  IdBinder idBinder();

  /**
   * Create a new entity bean instance.
   */
  EntityBean createEntityBean();

  /**
   * Create a new entity bean instance with option for read only optimisation.
   */
  EntityBean createEntityBean2(boolean readOnlyNoIntercept);

  /**
   * Put the entity bean into the persistence context.
   */
  Object contextPutIfAbsent(PersistenceContext persistenceContext, Object id, EntityBean localBean);

  /**
   * Invoke any post load listeners.
   */
  void postLoad(Object localBean);

  /**
   * Freeze the properties of the entity.
   */
  void freeze(EntityBean entityBean);

  /**
   * Return the base table to use given the temporalMode.
   */
  String baseTable(SpiQuery.TemporalMode temporalMode);

  /**
   * Return true if the given path is an embedded bean.
   */
  boolean isEmbeddedPath(String propertyPath);

  /**
   * Return the bean property traversing the object graph and taking into account inheritance.
   */
  STreeProperty findPropertyFromPath(String property);

  /**
   * Find a known property.
   */
  STreeProperty findProperty(String propName);

  /**
   * Find and return property allowing for dynamic formula properties.
   */
  STreeProperty findPropertyWithDynamic(String baseName, String path);

  /**
   * Return an extra join if the property path requires it.
   */
  ExtraJoin extraJoin(String propertyPath);

  /**
   * Load the property taking into account inheritance.
   */
  void inheritanceLoad(SqlBeanLoad sqlBeanLoad, STreeProperty property, DbReadContext ctx);

  /**
   * Mark the bean as deleted by setting the softDelete property to true.
   * This works also, if there is only a virtual softDelete property computed by a formula.
   * If there is no soft delete property, it sets the lazyLoadFailure flag in EBI.
   */
  void markAsDeleted(EntityBean bean);

  /**
   * Return the "path map" to toMany or toOne properties using the given prefix.
   */
  Map<String, String> pathMap(String prefix);
}
