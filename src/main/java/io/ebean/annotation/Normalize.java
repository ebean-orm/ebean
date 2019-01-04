package io.ebean.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field or a class with a Normalization.
 *
 * If the annotation is placed at class level. Normalization is done for all string properties
 * unless an other annotation is placed at field level.
 *
 * Multiple normalizers can be specified which will all be called one after the other.
 *
 * @author Alexander Wagner, FOCONIS AG
 *
 */
@Documented
@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface Normalize {

  /**
  * The Normalization class. Class must have a static 'T normalize(T input)' method.
  * T is the datatype of the field.
  */
  Class<?>[] value();

}
