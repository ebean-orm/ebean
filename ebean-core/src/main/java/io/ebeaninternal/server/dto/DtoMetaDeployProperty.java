package io.ebeaninternal.server.dto;

import io.ebean.annotation.MutationDetection;
import io.ebeaninternal.server.deploy.meta.DeployProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * DeployProperty for Dto-Properties.
 *
 * @author Roland Praml, FOCONIS AG
 */
class DtoMetaDeployProperty implements DeployProperty {
  private final String name;
  private final Class<?> ownerType;
  private final Type genericType;
  private final Class<?> propertyType;
  private final Set<Annotation> metaAnnotations;
  private final boolean nullable;
  private MutationDetection mutationDetection = MutationDetection.DEFAULT;

  DtoMetaDeployProperty(String name, Class<?> ownerType, Type genericType, Class<?> propertyType, Set<Annotation> metaAnnotations, Method method) {
    this.name = name;
    this.ownerType = ownerType;
    this.genericType = genericType;
    this.nullable = !propertyType.isPrimitive();
    this.propertyType = propertyType;
    this.metaAnnotations = metaAnnotations;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getGenericType() {
    return genericType;
  }

  @Override
  public Class<?> getPropertyType() {
    return propertyType;
  }

  @Override
  public Class<?> getOwnerType() {
    return ownerType;
  }

  @Override
  public <A extends Annotation> List<A> getMetaAnnotations(Class<A> annotationType) {
    List<A> result = new ArrayList<>();
    for (Annotation ann : metaAnnotations) {
      if (ann.annotationType() == annotationType) {
        result.add((A) ann);
      }
    }
    return result;
  }

  @Override
  public MutationDetection getMutationDetection() {
    return mutationDetection;
  }

  @Override
  public void setMutationDetection(MutationDetection mutationDetection) {
    this.mutationDetection = mutationDetection;
  }

  @Override
  public boolean isNullable() {
    return nullable;
  }
}
