package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a property holding JSON content.
 * <p>
 * By default the content will be stored in a DB Clob except on Postgres where DB JSON type is used.
 * </p>
 * <h3>Example:</h3>
 * <pre>{@code
 *
 * // Store as JSON on Postgres or Clob on other databases
 * @DbJson
 * Map<String,Object> content;
 *
 * }</pre>
 *
 * <h3>Example with JSONB storage</h3>
 * <pre>{@code
 *
 * // Store as JSONB on Postgres or Clob on other databases
 * @DbJson(storage = DbJsonType.JSONB)
 * Map<String,Object> content;
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbJson {

  /**
   * Specify the database type used to store the JSON content.
   */
  DbJsonType storage() default DbJsonType.JSON;

  /**
   * For VARCHAR storage specify the column length (defaults to 3000).
   */
  int length() default 0;
}