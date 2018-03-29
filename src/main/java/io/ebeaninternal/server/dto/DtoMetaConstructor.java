package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.DataReader;
import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.sql.SQLException;

class DtoMetaConstructor {

	private final Class<?>[] types;
	private final MethodHandle handle;
  private final ScalarType<?>[] scalarTypes;

	DtoMetaConstructor(TypeManager typeManager, Constructor<?> constructor, Class<?> someClass) throws NoSuchMethodException, IllegalAccessException {

    this.types = constructor.getParameterTypes();
    this.scalarTypes = new ScalarType[types.length];
    for (int i = 0; i < types.length; i++) {
      scalarTypes[i] = typeManager.getScalarType(types[i]);
    }

    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		this.handle = lookup.findConstructor(someClass, typeFor(types));
	}

	private MethodType typeFor(Class<?>[] types) {
		return MethodType.methodType(void.class, types);
	}

  Class<?>[] getTypes() {
    return types;
  }

  int getArgCount() {
    return types.length;
  }

  Object defaultConstructor() {
    try {
      return handle.invokeWithArguments();
    } catch (Throwable e) {
      throw new RuntimeException("Unexpected error invoking constructor", e);
    }
  }

	public Object process(DataReader dataReader) throws SQLException {
    Object[] values = new Object[scalarTypes.length];
    for (int i = 0; i < scalarTypes.length; i++) {
      values[i] = scalarTypes[i].read(dataReader);
    }
    return invoke(values);
  }

	private Object invoke(Object... args) {
	  try {
	    return handle.invokeWithArguments(args);
    } catch (Throwable e) {
	    throw new RuntimeException("Unexpected error invoking constructor", e);
    }
	}

}
