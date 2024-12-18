package io.ebeaninternal.server.dto;

import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.SQLException;

final class DtoMetaProperty implements DtoReadSet {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

  private final Class<?> dtoType;
  private final String name;
  private final MethodHandle setter;
  private final ScalarType<?> scalarType;

  DtoMetaProperty(TypeManager typeManager, Class<?> dtoType, Method writeMethod, String name) throws IllegalAccessException, NoSuchMethodException {
    this.dtoType = dtoType;
    this.name = name;
    if (writeMethod != null) {
      this.setter = lookupMethodHandle(dtoType, writeMethod);
      this.scalarType = typeManager.type(propertyType(writeMethod), propertyClass(writeMethod));
    } else {
      this.scalarType = null;
      this.setter = null;
    }
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
