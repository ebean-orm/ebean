package org.tests.model.aggregation;

import javax.persistence.Column;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Example meta annotation for <code>@Column</code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Column(precision = 9, scale = 3)
public @interface Decimal93 {
}
