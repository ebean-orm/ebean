package io.ebeaninternal.server.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class TypeReflectHelper {

  public static Class<?>[] getParams(Class<?> cls, Class<?> matchRawType) {
    return TypeResolver.resolveRawArgs(matchRawType, cls);
  }

  /**
   * Return the enum class for this type taking into account wildcard type.
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends Enum<?>> asEnumClass(Type valueType) {
    Class<?> enumClass = getClass(valueType);
    return (Class<? extends Enum<?>>) enumClass;
  }

  /**
   * Return true if the type is an enum.
   */
  public static boolean isEnumType(Type valueType) {
    try {
      return getClass(valueType).isEnum();
    } catch (ClassCastException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Return the type of the map key.
   */
  public static Class<?> getMapKeyType(Type genericType) {
    return getClass(getValueType(genericType));
  }

  /**
   * Return the value type of a collection type (list, set, map values).
   */
  public static Type getValueType(Type collectionType) {
    Type[] typeArgs = ((ParameterizedType) collectionType).getActualTypeArguments();
    return typeArgs[0];
  }

  private static Class<?> getClass(Type type) {

    while (true) {
      if (type instanceof ParameterizedType) {
        type = ((ParameterizedType) type).getRawType();
        continue;
      }
      if (type instanceof WildcardType) {
        Type[] upperBounds = ((WildcardType) type).getUpperBounds();
        if (upperBounds != null && upperBounds.length == 1) {
          return getClass(upperBounds[0]);
        }
        throw new IllegalArgumentException("Don't know how to determine Class from Type [" + type + "]");
      }
      return (Class<?>) type;
    }
  }
}
