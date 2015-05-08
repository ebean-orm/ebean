package com.avaje.ebean.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Helper to find classes taking into account the context class loader.
 */
public class ClassUtil {

  /**
   * Return a new instance of the class using the default constructor.
   */
  public static Object newInstance(String className) {
    try {
      Class<?> cls = Class.forName(className);
      return cls.newInstance();
    } catch (Exception e) {
      String msg = "Error constructing " + className;
      throw new IllegalArgumentException(msg, e);
    }
  }


  /**
   * Returns the raw type for the 2nd generic parameter for a subclass.
   */
  public static Class<?> getSecondArgumentType(Class<?> subclass) {
    Type[] typeArguments = getSuperclassTypeParameter(subclass);
    if (typeArguments.length != 2) {
      throw new IllegalArgumentException("Expected type with 2 generic argument types but got "
              + typeArguments.length + " - " + Arrays.toString(typeArguments));
    }

    return getRawType(typeArguments[1]);
  }

  static Type[] getSuperclassTypeParameter(Class<?> subclass) {
    Type superclass = subclass.getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing generics type parameters on subclass " + subclass);
    }
    return ((ParameterizedType) superclass).getActualTypeArguments();
  }

  private static Class<?> getRawType(Type type) {

    if (type instanceof Class<?>) {
      return (Class<?>) type;

    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?>) {
        return (Class<?>) rawType;
      }
    }
    throw new RuntimeException("Unable to obtain raw class type from " + type);
  }
}
