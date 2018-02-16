package io.ebeaninternal.server.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReflectHelper {

  public static Class<?>[] getParams(Class<?> cls, Class<?> matchRawType) {
    return TypeResolver.resolveRawArgs(matchRawType, cls);
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
}
