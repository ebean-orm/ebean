package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Update statement for a particular entity bean type.
 * <p>
 * The update can either be a sql insert,update or delete statement with tables
 * and columns etc or the equivalent statement but with table names and columns
 * expressed as bean types and bean properties.
 * </p>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedUpdate {

  /**
   * The name of the update.
   */
  String name();

  /**
   * The insert, update or delete statement.
   */
  String update();

  /**
   * Set this to false if you do not want the cache to be notified. If true the
   * cache will invalidate appropriate objects from the cache (after a
   * successful transaction commit).
   */
  boolean notifyCache() default true;

}
