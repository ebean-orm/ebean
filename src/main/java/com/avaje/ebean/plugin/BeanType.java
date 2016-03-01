package com.avaje.ebean.plugin;

import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebeanservice.docstore.api.mapping.DocumentMapping;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.util.Collection;

/**
 * Information and methods on BeanDescriptors made available to plugins.
 */
public interface BeanType<T> {

  /**
   * Return the full name of the bean type.
   */
  String getFullName();

  /**
   * Return the class type this BeanDescriptor describes.
   */
  Class<T> getBeanType();

  /**
   * Return the type bean for an OneToMany or ManyToOne or ManyToMany property.
   */
  BeanType<?> getBeanTypeAtPath(String propertyName);

  /**
   * Return all the properties for this bean type.
   */
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
   * Return the SpiProperty for a property to read values from a bean.
   */
  Property getProperty(String propertyName);

  /**
   * Return the SpiExpressionPath for a given property path.
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
   * Read the JSON content returning the bean.
   */
  T jsonRead(JsonParser parser, JsonReadOptions readOptions, Object objectMapper) throws IOException;

}
