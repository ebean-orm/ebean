package org.tests.model.virtualprop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
/**
 * Annotation for a field that is ignored when writing the changelog.
 *
 * @author Alexander Wagner, FOCONIS AG
 */
@Documented
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface VirtualOneToOne {

	Class<?> value();

	String propertyName() default "";

	boolean optional() default true;

	FetchType fetch() default FetchType.LAZY;

	CascadeType[] cascade() default { CascadeType.ALL };

}
