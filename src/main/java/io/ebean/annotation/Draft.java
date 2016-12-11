package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a boolean property on a @Draftable bean that indicates if the bean instance is a 'draft' or 'live' bean.
 * The property is transient and has no underlying DB column.
 * <p>
 * For beans returned from an <code>asDraft()</code> query this property will be set to true.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Draft {

}
