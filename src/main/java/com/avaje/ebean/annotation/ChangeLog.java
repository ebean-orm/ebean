package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity bean as being included in the change logging.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeLog {

  /**
   * Set this to true to exclude inserts on the associated bean type
   * from being included in the change log.
   */
  boolean excludeInserts() default false;

  /**
   * When specified only include update requests that have at least one
   * of the given properties as a dirty property.
   * <p>
   * This provides a way to filter requests to include in the change log such that
   * only updates that include at least one of the given properties is included
   * in the change log.
   * </p>
   */
  String[] updatesThatInclude() default {};
}
