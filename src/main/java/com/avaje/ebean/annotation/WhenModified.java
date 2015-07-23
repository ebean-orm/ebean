package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For a timestamp property that is set to the datetime when the entity was last modified.
 * <p>
 * This is effectively an alias for @UpdatedTimestamp and added to hint
 * towards a better naming convention (WhenCreated, WhenModified).
 * </p>
 * </p>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WhenModified {

}
