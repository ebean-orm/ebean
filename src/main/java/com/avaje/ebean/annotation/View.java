package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an entity bean with @View to indicates the bean is based on a view.
 * <p>
 * As such typically the view is defined in <code>extra-ddl.xml</code> using
 * <code>create or replace view ...</code>.
 * </p>
 * <p>
 * When using extra-ddl.xml Ebean will run the resulting DDL script after the
 * <code>create-all</code> DDL (which is typically used during development) and for
 * DB Migration will copy the scripts as <code>repeatable migration scripts</code> that
 * will be run by FlywayDb (or Ebean's own migration runner) when their MD5 hash changes.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {

  /**
   * The name of the view this entity bean is based on.
   */
  String name();

  /**
   * Tables this view is dependent on.
   * <p>
   * This is used with l2 caching to invalidate the query cache. Changes to these
   * tables invalidate the query cache for the entity based on this view.
   * </p>
   */
  String[] dependentTables() default {};
}
