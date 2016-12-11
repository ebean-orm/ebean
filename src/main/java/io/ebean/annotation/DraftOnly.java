package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property on a @Draftable bean that only exists on the 'draft' and not the 'live' table.
 * <p>
 * Typically this would be used on a property that is used as part of application 'workflow' such as
 * a publish workflow status or when publish timestamp.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DraftOnly {

}
