package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holds an array of named Update statements for a particular entity bean type.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedUpdates {

  /**
   * An array of named updates.
   */
  NamedUpdate[] value();

}
