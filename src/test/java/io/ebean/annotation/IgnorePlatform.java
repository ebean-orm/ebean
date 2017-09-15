package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.ebean.Platform;
/**
 * Annotation to ignore a test for a certain platform.
 * @author Roland Praml, FOCONIS AG
 */
@Target(ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnorePlatform {
  Platform[] value();
}
