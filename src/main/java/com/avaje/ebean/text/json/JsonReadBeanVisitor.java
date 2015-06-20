package com.avaje.ebean.text.json;


import java.util.Map;

/**
 * Provides for custom handling of json content as it is read.
 * <p>
 * This visit method is called after all the known properties of the bean have
 * been processed. Any JSON elements that could not be mapped to known bean
 * properties are available in the unmapped Map.
 * </p>
 *
 * @param <T> The type of entity bean
 */
public interface JsonReadBeanVisitor<T> {

  /**
   * Visit the bean that has just been processed.
   * <p>
   * This provides a method of customising the bean and processing any custom
   * JSON content.
   * </p>
   *
   * @param bean     the bean being processed
   * @param unmapped Map of any JSON elements that didn't map to known bean properties
   */
  void visit(T bean, Map<String, Object> unmapped);

}