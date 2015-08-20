package com.avaje.ebean.event.changelog;

/**
 * Used to assign ChangeLogFilters to bean types.
 * <p>
 * Ebean has a built in implementation that uses the ChangeLog annotation to build
 * appropriate ChangeLogFilters but you can provide an implementation to use instead.
 * </p>
 */
public interface ChangeLogRegister {

  /**
   * For the given bean type return the Change log filter to use.
   * <p>
   * This filter provides control over which persist request are included in the change log.
   */
  ChangeLogFilter getChangeFilter(Class<?> beanType);
}
