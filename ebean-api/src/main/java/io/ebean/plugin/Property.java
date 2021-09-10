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
  String name();

  /**
   * Deprecated migrate to name().
   */
  @Deprecated
  default String getName() {
    return name();
  }

  /**
   * Return the type of the property.
   */
  @Nonnull
  Class<?> type();

  /**
   * Deprecated migrate to type().
   */
  @Deprecated
  default Class<?> getPropertyType() {
    return type();
  }

  /**
   * Return the value of the property on the given bean.
   */
  Object value(Object bean);

  /**
   * Deprecated migrate to value().
   */
  @Deprecated
  default Object getVal(Object bean) {
    return value(bean);
  }

  /**
   * Return true if this is a OneToMany or ManyToMany property.
   */
  boolean isMany();
}
