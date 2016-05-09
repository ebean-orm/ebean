package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a collection property that will be stored into a DB ARRAY type.
 * <p>
 * If the target database does not support ARRAY type (so not Postgres)
 * then the collection will be stored in JSON format into a VARCHAR column.
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 *
 * // Store as ARRAY of UUID on Postgres
 * @DbArray
 * List<UUID> uids = new ArrayList<>();
 *
 * // Store as ARRAY on Postgres
 * @DbArray
 * List<String> phoneNumbers = new ArrayList<>();
 *
 * // Store as ARRAY of INTEGER on Postgres
 * @DbArray
 * List<Long> someLongs = new ArrayList<>();
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbArray {

  /**
   * For VARCHAR storage specify the column length (defaults to 1000).
   */
  int length() default 0;
}
