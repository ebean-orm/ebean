package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify that the elements of a OneToMany are private owned.
 * <p>
 * This means that if they are removed from the List/Set/Map they will be
 * deleted when their parent object is saved.
 * </p>
 * <p>
 * This could also be described as deleting orphans - in that beans removed from
 * the List/Set/Map will be deleted automatically when the parent bean is saved.
 * They are considered 'orphans' when they have been removed from the collection
 * in that they are no longer associated/linked to their parent bean.
 * </p>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivateOwned {

  /**
   * Set this to false if you don't want cascade REMOVE on this relationship.
   * <p>
   * That is, by default PrivateOwned implicitly adds a cascade REMOVE to the
   * relationship and if you don't want that you need to set this to false.
   * </p>
   */
  boolean cascadeRemove() default true;

}
