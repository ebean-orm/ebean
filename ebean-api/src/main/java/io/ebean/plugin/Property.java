package io.ebean.plugin;

/**
 * Property of a entity bean that can be read.
 */
public interface Property {

  /**
   * Return the name of the property.
   */
  String name();

  /**
   * Return the type of the property.
   */
  Class<?> type();

  /**
   * Return the value of the property on the given bean.
   */
  Object value(Object bean);

  /**
   * Return true if this is a OneToMany or ManyToMany property.
   */
  boolean isMany();
}
