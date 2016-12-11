package io.ebean.annotation;

import io.ebean.Query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the default cache use specific entity type.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

  /**
   * Set this to true to enable the use of query cache.
   * <p>
   * By default query caching is disabled as the query cache invalidates
   * frequently and so it is typically used for specific bean types and cases.
   * </p>
   */
  boolean enableQueryCache() default false;

  /**
   * Set this to false to disable the use of bean cache.
   * <p>
   * By default bean caching is expected so this defaults to true.  We might
   * set this to false on a bean type that we want to use query caching but no
   * bean caching (and this is expected to be a rare case).
   * </p>
   * <p>
   * When bean caching is enabled by default "find by id" and "find by unique natural key"
   * queries will try to use the bean cache. We use {@link Query#setUseCache(boolean)}
   * with <code>false</code> for the case when we do NOT want to use the bean cache.
   * </p>
   */
  boolean enableBeanCache() default true;

  /**
   * Specify the property that is a natural unique identifier for the bean.
   * <p>
   * When a findUnique() query is used with this property as the sole expression
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

}
