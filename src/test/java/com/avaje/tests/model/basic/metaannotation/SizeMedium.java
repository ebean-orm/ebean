package com.avaje.tests.model.basic.metaannotation;

import javax.validation.constraints.Size;
import java.lang.annotation.*;

/**
 * Meta-Annotation that defines &#64;Size(max=100)
 *
 * @author Roland Praml, FOCONIS AG
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = 100)
public @interface SizeMedium {
}
