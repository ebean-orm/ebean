package io.ebeaninternal.server.dto;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployProperty;
import io.ebeaninternal.server.deploy.parse.DeployUtil;
import io.ebeaninternal.server.type.TypeManager;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class DtoMetaProperty implements DtoReadSet {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

  private final Class<?> dtoType;
  private final String name;
  private final MethodHandle setter;
  private final ScalarType<?> scalarType;

  DtoMetaProperty(TypeManager typeManager, Class<?> dtoType, Method writeMethod, String name, Set<Class<?>> annotationFilter)
    throws IllegalAccessException, NoSuchMethodException {
    this.dtoType = dtoType;
    this.name = name;
    if (writeMethod != null) {
      this.setter = lookupMethodHandle(dtoType, writeMethod);
      Field field = findField(dtoType, name);
      DeployProperty deployProp = new DtoMetaDeployProperty(name,
        dtoType,
        propertyType(writeMethod),
        propertyClass(writeMethod),
        field == null ? Collections.emptySet() : AnnotationUtil.metaFindAllFor(field, annotationFilter),
        writeMethod);
      scalarType = getScalarType(typeManager, deployProp);
    } else {
      this.scalarType = null;
      this.setter = null;
    }
  }

  private ScalarType<?> getScalarType(TypeManager typeManager, DeployProperty deployProp) {
    final ScalarType<?> scalarType;

    List<DbJson> json = deployProp.getMetaAnnotations(DbJson.class);
    if (!json.isEmpty()) {
      return typeManager.dbJsonType(deployProp, DeployUtil.dbJsonStorage(json.get(0).storage()), json.get(0).length());
    }
    List<DbJsonB> jsonB = deployProp.getMetaAnnotations(DbJsonB.class);
    if (!jsonB.isEmpty()) {
      return typeManager.dbJsonType(deployProp, DbPlatformType.JSONB, jsonB.get(0).length());
    }
    if (typeManager.jsonMarkerAnnotation() != null
      && !deployProp.getMetaAnnotations(typeManager.jsonMarkerAnnotation()).isEmpty()) {
      return typeManager.dbJsonType(deployProp, DbPlatformType.JSON, 0);
    }
    return typeManager.type(deployProp);


  }

  /**
   * Find all annotations on fields and methods.
   */
  private Set<Annotation> findMetaAnnotations(Class<?> dtoType, Method writeMethod, String name, Set<Class<?>> annotationFilter) {
    Field field = findField(dtoType, name);
    if (field != null) {
      Set<Annotation> metaAnnotations = AnnotationUtil.metaFindAllFor(field, annotationFilter);
      metaAnnotations.addAll(AnnotationUtil.metaFindAllFor(writeMethod, annotationFilter));
      return metaAnnotations;
    } else {
      return AnnotationUtil.metaFindAllFor(writeMethod, annotationFilter);
    }
  }

  /**
   * Find field in class with same name
   */
  private Field findField(Class<?> type, String name) {
    while (type != Object.class && type != null) {
      try {
        return dtoType.getDeclaredField(name);
      } catch (NoSuchFieldException e) {
        type = type.getSuperclass();
      }
    }
    return null;
  }

  private static MethodHandle lookupMethodHandle(Class<?> dtoType, Method method) throws NoSuchMethodException, IllegalAccessException {
    return LOOKUP.findVirtual(dtoType, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
  }

  static Type propertyType(Method method) {
    return method.getParameters()[0].getParameterizedType();
  }

  static Class<?> propertyClass(Method method) {
    return method.getParameterTypes()[0];
  }

  String name() {
    return name;
  }

  @Override
  public boolean isReadOnly() {
    return scalarType == null;
  }

  @Override
  public void readSet(Object bean, DataReader dataReader) throws SQLException {
    Object value = scalarType.read(dataReader);
    invoke(bean, value);
  }

  private void invoke(Object instance, Object arg) {
    try {
      setter.invoke(instance, arg);
    } catch (Throwable e) {
      throw new RuntimeException("Error calling setter for property " + fullName() + " with arg: " + arg, e);
    }
  }

  private String fullName() {
    return dtoType.getName() + "." + name;
  }

}
