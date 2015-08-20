package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity bean as being included in the change logging.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeLog {

  /**
   * Set this to true to exclude inserts on the associated bean type
   * from being included in the change log.
   */
  boolean excludeInserts() default false;

}
