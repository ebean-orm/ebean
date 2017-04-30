package io.ebeaninternal.server.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReflectHelper {

  public static Class<?>[] getParams(Class<?> cls, Class<?> matchRawType) {

    Type[] types = getParamType(cls, matchRawType);
    Class<?>[] result = new Class<?>[types.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = getClass(types[i]);
    }
    return result;
  }

  public static Class<?> getClass(Type type) {

    while (true) {
      if (type instanceof ParameterizedType) {
        type = ((ParameterizedType) type).getRawType();
        continue;
      }

      return (Class<?>) type;
    }
  }

  private static Type[] getParamType(Class<?> cls, Class<?> matchRawType) {

    Type[] gis = cls.getGenericInterfaces();
    for (Type type : gis) {
      if (type instanceof ParameterizedType) {
        ParameterizedType paramType = (ParameterizedType) type;
        Type rawType = paramType.getRawType();
        if (rawType.equals(matchRawType)) {

          return paramType.getActualTypeArguments();
        }
      }
    }
    return null;
  }
}
