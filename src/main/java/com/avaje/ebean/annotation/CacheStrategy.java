package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avaje.ebean.Query;

/**
 * Specify the default cache use specific entity type.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheStrategy {

  /**
   * When set to true the bean cache will be used unless explicitly stated not
   * to in a query via {@link Query#setUseCache(boolean)}.
   */
  boolean useBeanCache() default true;

  /**
   * A single property that is a natural unique identifier for the bean.
   * <p>
   * When a findUnique query is used with this property as the sole expression
   * then there will be a lookup into the L2 natural key cache.
   * </p>
   */
  String naturalKey() default "";

  /**
   * When set to true the beans returned from a query will default to be
   * readOnly.
   * <p>
   * If the bean is readOnly and has no relationships then it may be sharable.
   * </p>
   * <p>
   * If you try to modify a readOnly bean it will throw an
   * IllegalStateException.
   * </p>
   */
  boolean readOnly() default false;

  /**
   * Specify a query that can be used to warm the cache.
   * <p>
   * All the beans fetched by this query will be loaded into the bean cache and
   * the query itself will be loaded into the query cache.
   * </p>
   * <p>
   * The warming query will typically be executed at startup time after a short
   * delay (defaults to a 30 seconds delay).
   * </p>
   */
  String warmingQuery() default "";

}
