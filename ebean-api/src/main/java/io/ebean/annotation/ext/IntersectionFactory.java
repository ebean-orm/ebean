package io.ebean.annotation.ext;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to define a factory for an intersection model. This class MUST have a constructor or factory method with two parameters that accepts parent and property type.
 * @author Roland Praml, FOCONIS AG
 */
@Documented
@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface IntersectionFactory {

  /**
   * The intersection model class.
   */
  Class value();

  /**
   * An optional factory method.
   */
  String factoryMethod() default "";
}
