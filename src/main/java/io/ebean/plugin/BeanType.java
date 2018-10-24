package io.ebean.plugin;

import io.ebean.Query;
import io.ebean.config.dbplatform.IdType;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanQueryAdapter;
import io.ebeanservice.docstore.api.mapping.DocumentMapping;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * Information and methods on BeanDescriptors made available to plugins.
 */
public interface BeanType<T> {

  /**
   * Return the short name of the bean type.
   */
  @Nonnull
  String getName();

  /**
   * Return the profileId of the bean type.
   */
  short getProfileId();

  /**
   * Return the full name of the bean type.
   */
  @Nonnull
  String getFullName();

  /**
   * Return the class type this BeanDescriptor describes.
   */
  @Nonnull
  Class<T> getBeanType();

  /**
   * Return the type bean for an OneToMany or ManyToOne or ManyToMany property.
   */
  BeanType<?> getBeanTypeAtPath(String propertyName);

  /**
   * Return all the properties for this bean type.
   */
  @Nonnull
  Collection<? extends Property> allProperties();

  /**
   * Return the Id property.
   */
  Property getIdProperty();

  /**
   * Return the when modified property if there is one defined.
   */
  Property getWhenModifiedProperty();

  /**
   * Return the when created property if there is one defined.
   */
  Property getWhenCreatedProperty();

  /**
   * Return the Property to read values from a bean.
   */
  Property getProperty(String propertyName);

  /**
   * Return the ExpressionPath for a given property path.
   * <p>
   * This can return a property or nested property path.
   * </p>
   */
  ExpressionPath getExpressionPath(String path);

  /**
   * Return true if the property is a valid known property or path for the given bean type.
   */
  boolean isValidExpression(String property);

  /**
   * Return true if bean caching is on for this bean type.
   */
  boolean isBeanCaching();

  /**
   * Return true if query caching is on for this bean type.
   */
  boolean isQueryCaching();

  /**
   * Clear the bean cache.
   */
  void clearBeanCache();

  /**
   * Clear the query cache.
   */
  void clearQueryCache();

  /**
   * Return true if the type is document store only.
   */
  boolean isDocStoreOnly();

  /**
   * Return the base table this bean type maps to.
   */
  String getBaseTable();

  /**
   * Create a new instance of the bean.
   */
  T createBean();

  /**
   * Return the bean id. This is the same as getBeanId() but without the generic type.
   */
  Object beanId(Object bean);

  /**
   * Return the id value for the given bean.
   */
  Object getBeanId(T bean);

  /**
   * Set the id value to the bean.
   */
  void setBeanId(T bean, Object idValue);

  /**
   * Return the bean persist controller.
   */
  BeanPersistController getPersistController();

  /**
   * Return the bean persist listener.
   */
  BeanPersistListener getPersistListener();

  /**
   * Return the beanFinder. Usually null unless overriding the finder.
   */
  BeanFindController getFindController();

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  BeanQueryAdapter getQueryAdapter();

  /**
   * Return the identity generation type.
   */
  IdType getIdType();

  /**
   * Return the sequence name associated to this entity bean type (if there is one).
   */
  String getSequenceName();

  /**
   * Return true if this bean type has doc store backing.
   */
  boolean isDocStoreMapped();

  /**
   * Return the DocumentMapping for this bean type.
   * <p>
   * This is the document structure and mapping options for how this bean type is mapped
   * for the document store.
   * </p>
   */
  DocumentMapping getDocMapping();

  /**
   * Return the doc store queueId for this bean type.
   */
  String getDocStoreQueueId();

  /**
   * Return the doc store support for this bean type.\
   */
  BeanDocType<T> docStore();

  /**
   * Add the discriminator value to the query if needed.
   */
  void addInheritanceWhere(Query<?> query);

  /**
   * Return the root bean type for an inheritance hierarchy.
   */
  BeanType<?> root();

  /**
   * Return true if this bean type has an inheritance hierarchy.
   */
  boolean hasInheritance();

  /**
   * Return true if this object is the root level object in its entity
   * inheritance.
   */
  boolean isInheritanceRoot();

  /**
   * Returns all direct children of this beantype
   */
  List<BeanType<?>> getInheritanceChildren();

  /**
   * Returns the parent in inheritance hiearchy
   */
  BeanType<?> getInheritanceParent();

  /**
   * Visit all children recursively
   * @param visitor
   */
  void visitAllInheritanceChildren(Consumer<BeanType<?>> visitor);

  /**
   * Return the discriminator column.
   */
  String getDiscColumn();

  /**
   * Create a bean given the discriminator value.
   */
  T createBeanUsingDisc(Object discValue);
}
