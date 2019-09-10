package io.ebean.plugin;

import javax.annotation.Nonnull;

/**
 * Property of a entity bean that can be read.
 */
public interface Property {

  /**
   * Return the name of the property.
   */
  @Nonnull
  String getName();

  /**
   * Return the type of the property.
   */
  @Nonnull
  Class<?> getPropertyType();

  /**
   * Return the value of the property on the given bean.
   */
  Object getVal(Object bean);

  /**
   * Return true if this is a OneToMany or ManyToMany property.
   */
  boolean isMany();
}
