package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.DataReader;
import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;

import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.sql.SQLException;

class DtoMetaProperty implements DtoReadSet {

  private final Class<?> dtoType;
  private final String name;
  private final MethodHandle setter;
  private final ScalarType<?> scalarType;

  DtoMetaProperty(TypeManager typeManager, PropertyDescriptor descriptor, Class<?> dtoType) throws IllegalAccessException, NoSuchMethodException {

    this.dtoType = dtoType;
    this.name = descriptor.getName();

    Method writeMethod = descriptor.getWriteMethod();
    if (writeMethod != null) {

      Class<?> propertyType = descriptor.getPropertyType();

      MethodHandles.Lookup lookup = MethodHandles.publicLookup();
      this.setter = lookup.findVirtual(dtoType, writeMethod.getName(), MethodType.methodType(void.class, propertyType));
      this.scalarType = typeManager.getScalarType(propertyType);

    } else {
      this.scalarType = null;
      this.setter = null;
    }
  }

  String getName() {
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
      throw new RuntimeException("Error calling setter for property " + fullname() + " with arg: " + arg, e);
    }
  }

  private String fullname() {
    return dtoType.getName() + "." + name;
  }

}
