package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables you to specify a value to use to persist for an enum value.
 * 
 * <pre>{@code
 *
 * public enum Status {
 *
 *   @EnumValue("N")
 *   NEW,
 * 
 *   @EnumValue("A")
 *   ACTIVE,
 * 
 *   @EnumValue("I")
 *   INACTIVE,
 * }
 * 
 * }</pre>
 * <p>
 * This is an alternative to using the JPA standard approach or Ebean's
 * {@link EnumMapping} annotation.
 * </p>
 * <p>
 * Note that if all the EnumValue values are parsable as Integers then Ebean
 * will persist and fetch them as integers - otherwise they will be persisted
 * and fetched as strings.
 * </p>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {

  /**
   * Specify the value to persist for a specific enum value.
   * <p>
   * If all the values are parsable as Integers then Ebean will persist and
   * fetch them as integers rather than strings.
   * </p>
   */
  String value();
}
