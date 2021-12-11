package io.ebean.plugin;

import io.ebean.Query;
import io.ebean.config.dbplatform.IdType;
import io.ebean.docstore.DocMapping;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanQueryAdapter;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Information and methods on BeanDescriptors made available to plugins.
 */
public interface BeanType<T> {

  /**
   * Return the short name of the bean type.
   */
  String name();

  /**
   * Deprecated migrate to name().
   */
  @Deprecated
  default String getName() {
    return name();
  }

  /**
   * Return the full name of the bean type.
   */
  String fullName();

  /**
   * Deprecated migrate to fullName().
   */
  @Deprecated
  default String getFullName() {
    return fullName();
  }

  /**
   * Return the class type this BeanDescriptor describes.
   */
  Class<T> type();

  /**
   * Deprecated migrate to type().
   */
  @Deprecated
  default Class<T> getBeanType() {
    return type();
  }

  /**
   * Return the type bean for an OneToMany or ManyToOne or ManyToMany property.
   */
  BeanType<?> beanTypeAtPath(String propertyName);

  /**
   * Deprecated migrate to beanTypeAtPath().
   */
  @Deprecated
  default BeanType<?> getBeanTypeAtPath(String propertyName) {
    return beanTypeAtPath(propertyName);
  }

  /**
   * Return all the properties for this bean type.
   */
  Collection<? extends Property> allProperties();

  /**
   * Return the Id property.
   */
  Property idProperty();

  /**
   * Deprecated migrate to idProperty().
   */
  @Deprecated
  default Property getIdProperty() {
    return idProperty();
  }

  /**
   * Return the when modified property if there is one defined.
   */
  Property whenModifiedProperty();

  /**
   * Deprecated migrate to idProperty().
   */
  @Deprecated
  default Property getWhenModifiedProperty() {
    return whenModifiedProperty();
  }

  /**
   * Return the when created property if there is one defined.
   */
  Property whenCreatedProperty();

  /**
   * Deprecated migrate to idProperty().
   */
  @Deprecated
  default Property getWhenCreatedProperty() {
    return whenCreatedProperty();
  }

  /**
   * Return the Property to read values from a bean.
   */
  Property property(String propertyName);

  /**
   * Deprecated migrate to property().
   */
  @Deprecated
  default Property getProperty(String propertyName) {
    return property(propertyName);
  }

  /**
   * Return the ExpressionPath for a given property path.
   * <p>
   * This can return a property or nested property path.
   * </p>
   */
  ExpressionPath expressionPath(String path);

  /**
   * Deprecated migrate to expressionPath().
   */
  @Deprecated
  default ExpressionPath getExpressionPath(String path) {
    return expressionPath(path);
  }

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
   * Deprecated migrate to baseTable().
   */
  @Deprecated
  default String getBaseTable() {
    return baseTable();
  }

  /**
   * Create a new instance of the bean.
   */
  T createBean();

  /**
   * Return the bean id. This is the same as getBeanId() but without the generic type.
   */
  Object id(Object bean);

  /**
   * Deprecated migrate to id()
   */
  @Deprecated
  default Object beanId(Object bean) {
    return id(bean);
  }

  /**
   * Deprecated migrate to id()
   */
  @Deprecated
  Object getBeanId(T bean);

  /**
   * Set the id value to the bean.
   */
  void setId(T bean, Object idValue);

  /**
   * Deprecated migrate to setId()
   */
  @Deprecated
  default void setBeanId(T bean, Object idValue) {
    setId(bean, idValue);
  }

  /**
   * Return the bean persist controller.
   */
  BeanPersistController persistController();

  /**
   * Deprecated migrate to persistController()
   */
  @Deprecated
  default BeanPersistController getPersistController() {
    return persistController();
  }

  /**
   * Return the bean persist listener.
   */
  BeanPersistListener persistListener();

  /**
   * Deprecated migrate to persistListener()
   */
  @Deprecated
  default BeanPersistListener getPersistListener() {
    return persistListener();
  }

  /**
   * Return the beanFinder. Usually null unless overriding the finder.
   */
  BeanFindController findController();

  /**
   * Deprecated migrate to findController()
   */
  @Deprecated
  default BeanFindController getFindController() {
    return findController();
  }

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  BeanQueryAdapter queryAdapter();

  /**
   * Deprecated migrate to queryAdapter()
   */
  @Deprecated
  default BeanQueryAdapter getQueryAdapter() {
    return queryAdapter();
  }

  /**
   * Return the identity generation type.
   */
  IdType idType();

  /**
   * Deprecated migrate to idType()
   */
  @Deprecated
  default IdType getIdType() {
    return idType();
  }

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
  DocMapping docMapping();

  /**
   * Deprecated migrate to docMapping()
   */
  @Deprecated
  default DocMapping getDocMapping() {
    return docMapping();
  }

  /**
   * Return the doc store queueId for this bean type.
   */
  String docStoreQueueId();

  /**
   * Deprecated migrate to docStoreQueueId()
   */
  @Deprecated
  default String getDocStoreQueueId() {
    return docStoreQueueId();
  }

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
  List<BeanType<?>> inheritanceChildren();

  /**
   * Deprecated migrate to inheritanceChildren()
   */
  @Deprecated
  default List<BeanType<?>> getInheritanceChildren() {
    return inheritanceChildren();
  }

  /**
   * Returns the parent in inheritance hierarchy
   */
  BeanType<?> inheritanceParent();

  /**
   * Deprecated migrate to inheritanceParent()
   */
  @Deprecated
  default BeanType<?> getInheritanceParent() {
    return inheritanceParent();
  }

  /**
   * Visit all children recursively
   */
  void visitAllInheritanceChildren(Consumer<BeanType<?>> visitor);

  /**
   * Return the discriminator column.
   */
  String discColumn();

  /**
   * Deprecated migrate to discColumn()
   */
  @Deprecated
  default String getDiscColumn() {
    return discColumn();
  }

  /**
   * Create a bean given the discriminator value.
   */
  T createBeanUsingDisc(Object discValue);
}
