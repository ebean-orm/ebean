package io.ebeaninternal.server.deploy.meta;

import io.ebean.annotation.MutationDetection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Property, with basic type information (BeanProperty and DtoProperty).
 */
public interface DeployProperty {

  /**
   * Return the name of the property.
   */
  String name();

  /**
   * Return the generic type for this property.
   */
  Type genericType();

  /**
   * Return the property type.
   */
  Class<?> propertyType();

  /**
   * Returns the owner class of this property.
   */
  Class<?> ownerType();

  /**
   * Returns the annotations on this property.
   */
  <A extends Annotation> List<A> metaAnnotations(Class<A> annotationType);

  /**
   * Returns the mutation detection setting of this property.
   */
  MutationDetection mutationDetection();

  /**
   * Sets the mutation detection setting of this property.
   */
  void setMutationDetection(MutationDetection mutationDetection);

  /**
   * Return true if this property is not mandatory.
   */
  boolean isNullable();
}
