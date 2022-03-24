package io.ebean.xtest;

import io.ebean.annotation.Platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to ignore a test for a certain platform.
 * @author Roland Praml, FOCONIS AG
 */
@Target(ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnorePlatform {
  Platform[] value();
}
