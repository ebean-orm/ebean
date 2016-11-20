package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;

/**
 * Add an Literal to add to the where clause when a many property (List, Set or
 * Map) is loaded or refreshed.
 * <pre>{@code
 *
 * // on a OneToMany property...
 *
 * @OneToMany
 * @Where(clause = "deleted='y'")
 * List<Topic> topics;
 *
 * }</pre>
 * <p>
 * Note that you can include "${ta}" as a place holder for the table alias if
 * you need to include the table alias in the clause.
 * </p>
 * <pre>{@code
 * // ... including the ${ta} table alias placeholder...
 *
 * @OneToMany
 * @Where(clause = "${ta}.deleted='y'")
 * List<Topic> topics;
 *
 * }</pre>
 * <p>
 * This will be added to the where clause when lazy loading the OneToMany
 * property or when there is a join to that OneToMany property.
 * </p>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Where.List.class)
public @interface Where {

  /**
   * The clause added to the lazy load query.
   * <p>
   * Note that you can include "${ta}" as a place holder for the table alias if
   * you need to include the table alias in the clause.
   * </p>
   */
  String clause();

  /**
   * The platform where this annotation is active. Default: any platform
   */
  Class<? extends DatabasePlatform>[] platforms() default {};

  /**
   * Repeatable support for {@link Where}.
   */
  @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {

    Where[] value() default {};
  }
}
