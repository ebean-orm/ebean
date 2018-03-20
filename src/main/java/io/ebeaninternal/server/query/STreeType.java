package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.id.IdBinder;

/**
 * Bean type interface for Sql query tree.
 */
public interface STreeType {

  /**
   * Return the bean short name.
   */
  String getName();

  /**
   * Return true if the underlying type has an Id property.
   */
  boolean hasId();

  /**
   * Return true if the type is for ElementCollection (not mapped to an entity type/class).
   */
  boolean isElementType();

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
  String getSoftDeletePredicate(String baseTableAlias);

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
   * Return the inheritance information for this type.
   */
  InheritInfo getInheritInfo();

  /**
   * Return the IdBinder for this type.
   */
  IdBinder getIdBinder();

  /**
   * Create a new entity bean instance.
   */
  EntityBean createEntityBean();

  /**
   * Put the entity bean into the persistence context.
   */
  Object contextPutIfAbsent(PersistenceContext persistenceContext, Object id, EntityBean localBean);

  /**
   * Set draft status on the entity bean.
   */
  void setDraft(EntityBean localBean);

  /**
   * Invoke any post load listeners.
   */
  void postLoad(Object localBean);

  /**
   * Return the base table to use given the temporalMode.
   */
  String getBaseTable(SpiQuery.TemporalMode temporalMode);

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
  STreeProperty findPropertyWithDynamic(String baseName);

  /**
   * Return an extra join if the property path requires it.
   */
  ExtraJoin extraJoin(String propertyPath);

  /**
   * Load the property taking into account inheritance.
   */
  void inheritanceLoad(SqlBeanLoad sqlBeanLoad, STreeProperty property, DbReadContext ctx);


}
