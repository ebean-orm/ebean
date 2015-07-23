package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For a timestamp property that is set to the datetime when the entity is
 * created/inserted.
 * <p>
 * This is effectively an alias for @CreatedTimestamp and added to hint
 * towards a better naming convention (WhenCreated, WhenModified).
 * </p>
 * <p>
 * An alternative to using this annotation would be to use insertable=false,
 * updateable=false with @Column and have the DB insert the current time
 * (default value on the DB column is SYSTIME etc).
 * </p>
 * <p>
 * The downside to this approach is that the inserted entity does not have the
 * timestamp value after the insert has occurred. You need to fetch the entity
 * back to get the inserted timestamp if you want to used it.
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 *
 *   @WhenCreated
 *   Timestamp whenCreated;
 *
 * }</pre>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WhenCreated {

}
