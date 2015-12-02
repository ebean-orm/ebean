package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate a property on an entity bean used to control 'soft delete'
 * (also known as 'logical delete').
 * <p>
 * The property should be of type boolean.
 * </p>
 * <pre>{@code
 *
 * @SoftDelete
 * boolean deleted;
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SoftDelete {

}
