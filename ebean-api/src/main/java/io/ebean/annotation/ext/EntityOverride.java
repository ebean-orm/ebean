package io.ebean.annotation.ext;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to define that an entity overrides the parent entity.
 * @author Roland Praml, FOCONIS AG
 */
@Documented
@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface EntityOverride {

  /**
   * The priority of the statement. Lower priority wins.
   */
  int priority() default 0;

}
