package io.ebean.plugin;

import io.ebean.Query;
import io.ebean.config.dbplatform.IdType;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanQueryAdapter;

import java.util.Collection;

/**
 * Information and methods on BeanDescriptors made available to plugins.
 */
public interface BeanType<T> {

  /**
   * Return the short name of the bean type.
   */
  String name();

  /**
   * Return the full name of the bean type.
   */
  String fullName();

  /**
   * Return the class type this BeanDescriptor describes.
   */
  Class<T> type();

  /**
   * Return the type bean for an OneToMany or ManyToOne or ManyToMany property.
   */
  BeanType<?> beanTypeAtPath(String propertyName);

  /**
   * Return all the properties for this bean type.
   */
  Collection<? extends Property> allProperties();

  /**
   * Return the Id property.
   */
  Property idProperty();

  /**
   * Return the when modified property if there is one defined.
   */
  Property whenModifiedProperty();

  /**
   * Return the when created property if there is one defined.
   */
  Property whenCreatedProperty();

  /**
   * Return the Property to read values from a bean.
   */
  Property property(String propertyName);

  /**
   * Return the ExpressionPath for a given property path.
   * <p>
   * This can return a property or nested property path.
   * </p>
   */
  ExpressionPath expressionPath(String path);

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
  String baseTable();

  /**
   * Create a new instance of the bean.
   */
  T createBean();

  /**
   * Return the bean id. This is the same as getBeanId() but without the generic type.
   */
  Object id(Object bean);

  /**
   * Set the id value to the bean.
   */
  void setId(T bean, Object idValue);

  /**
   * Return the bean persist controller.
   */
  BeanPersistController persistController();

  /**
   * Return the bean persist listener.
   */
  BeanPersistListener persistListener();

  /**
   * Return the beanFinder. Usually null unless overriding the finder.
   */
  BeanFindController findController();

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  BeanQueryAdapter queryAdapter();

  /**
   * Return the identity generation type.
   */
  IdType idType();

}
