package io.ebean.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by generated code to hold entity classes to register with Ebean.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {

  /**
   * Returns the entity classes with db name prefix.
   * <p>
   * The db name prefix is added to entity classes for non default databases.
   */
  String[] entities() default {};

  /**
   * Other classes like Attribute Converters.
   */
  String[] other() default {};
}
