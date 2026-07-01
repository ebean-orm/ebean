package io.ebeaninternal.server.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

public final class TypeReflectHelper {

  /**
   * Return the full map of TypeVariable to resolved Type for the given class and its entire
   * superclass/interface hierarchy. Used to resolve generic field types in mapped superclasses.
   */
  public static Map<TypeVariable<?>, Type> typeVariableMap(Class<?> targetType) {
    return TypeResolver.getTypeVariableMap(targetType);
  }

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
   * Return the raw type of the map key (first type argument).
   */
  public static Type getMapKeyTypeRaw(Type genericType) {
    Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
    return typeArgs[0];
  }

  /**
   * Return the value type of a collection type (list, set, map values).
   */
  public static Type getValueType(Type collectionType) {
    Type[] typeArgs = ((ParameterizedType) collectionType).getActualTypeArguments();
    return typeArgs[0];
  }

  /**
   * Return the raw Class for a Type, handling Class, ParameterizedType, and WildcardType.
   * Returns null when the raw class cannot be determined.
   */
  public static Class<?> resolveToClass(Type type) {
    if (type instanceof Class) {
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      Type raw = ((ParameterizedType) type).getRawType();
      if (raw instanceof Class) {
        return (Class<?>) raw;
      }
    }
    if (type instanceof WildcardType) {
      Type[] upperBounds = ((WildcardType) type).getUpperBounds();
      if (upperBounds.length == 1) {
        return resolveToClass(upperBounds[0]);
      }
    }
    return null;
  }

  /**
   * Return the element class for a collection or map type argument (handles Class,
   * ParameterizedType raw, and Kotlin-generated wildcard upper bounds).
   */
  public static Class<?> resolveCollectionTarget(Type typeArg) {
    if (typeArg instanceof Class) {
      return (Class<?>) typeArg;
    }
    if (typeArg instanceof ParameterizedType) {
      Type rawType = ((ParameterizedType) typeArg).getRawType();
      if (rawType instanceof Class) {
        return (Class<?>) rawType;
      }
      return null;
    }
    if (typeArg instanceof WildcardType) {
      // kotlin generated wildcard type
      Type[] upperBounds = ((WildcardType) typeArg).getUpperBounds();
      if (upperBounds.length == 1) {
        return resolveToClass(upperBounds[0]);
      }
    }
    return null;
  }

  /**
   * Resolve a Type through the given type-variable map, recursively substituting any bound
   * TypeVariables and rebuilding ParameterizedTypes whose arguments changed.
   */
  public static Type resolveType(Type type, Map<TypeVariable<?>, Type> typeMap) {
    if (type instanceof TypeVariable) {
      TypeVariable<?> typeVariable = (TypeVariable<?>) type;
      Type resolved = typeMap.get(typeVariable);
      return resolved != null ? resolveType(resolved, typeMap) : typeVariable;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] actualArgs = parameterizedType.getActualTypeArguments();
      Type[] resolvedArgs = new Type[actualArgs.length];
      boolean changed = false;
      for (int i = 0; i < actualArgs.length; i++) {
        resolvedArgs[i] = resolveType(actualArgs[i], typeMap);
        changed |= resolvedArgs[i] != actualArgs[i];
      }
      if (!changed) {
        return parameterizedType;
      }
      return new ResolvedParameterizedType(
        parameterizedType.getOwnerType(),
        parameterizedType.getRawType(),
        resolvedArgs
      );
    }
    return type;
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
        throw new IllegalArgumentException("Don't know how to determine Class from type " + type);
      }
      return (Class<?>) type;
    }
  }

  private static final class ResolvedParameterizedType implements ParameterizedType {
    private final Type ownerType;
    private final Type rawType;
    private final Type[] actualTypeArguments;

    ResolvedParameterizedType(Type ownerType, Type rawType, Type[] actualTypeArguments) {
      this.ownerType = ownerType;
      this.rawType = rawType;
      this.actualTypeArguments = actualTypeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return actualTypeArguments.clone();
    }

    @Override
    public Type getRawType() {
      return rawType;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }
  }
}
