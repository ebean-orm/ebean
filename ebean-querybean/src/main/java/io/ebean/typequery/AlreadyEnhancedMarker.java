package io.ebean.typequery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote a query bean that has already been enhanced.
 * <p>
 * Used by the agent to detect already enhanced type query beans to skip enhancement processing.
 * </p>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AlreadyEnhancedMarker {

}
