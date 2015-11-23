package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a boolean property on a @Draftable bean that only exists on the 'draft' table
 * and is used to detect when a draft has unpublished changes.
 * <p>
 * This property will automatically have it's value set to true when a draft is saved and
 * automatically have it's value set to false when the bean is published.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DraftDirty {

}
