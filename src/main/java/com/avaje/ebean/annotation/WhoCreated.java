package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mapped onto a entity bean property that represents the user id of
 * who created the entity>
 * <p>
 * To use this annotation you need to implement CurrentUserProvider.
 * The type of the bean property should match the type returned by
 * CurrentUserProvider.
 *</p>

 * <h3>Example:</h3>
 * <pre>{@code
 *
 *   @WhoCreated
 *   String whoCreated;
 *
 * }</pre>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WhoCreated {

}
