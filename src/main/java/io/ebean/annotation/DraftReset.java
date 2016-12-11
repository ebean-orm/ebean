package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property on a @Draftable bean that is set to null on the 'draft bean' on publish.
 * <p>
 * This is expected to be put on properties that get 'reset' or 'cleared' after a publish.
 * These properties might represent a publish comment or publish timestamp.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DraftReset {

}
