package io.ebeaninternal.server.type;


import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a modified version of TypeResolver from https://github.com/jhalterman/typetools
 * which is Apache 2 license.
 *
 * It is a cut down version removing the lambda support and related sun.misc.Unsafe use etc.
 */
class TypeResolver {

  /** An unknown type. */
  private static final class Unknown {
    private Unknown() {
    }
  }

  private TypeResolver() {
  }

  /**
   * Returns an array of raw classes representing arguments for the {@code type} using type variable information from
   * the {@code subType}. Arguments for {@code type} that cannot be resolved are returned as {@code Unknown.class}. If
   * no arguments can be resolved then {@code null} is returned.
   *
   * @param type to resolve arguments for
   * @param subType to extract type variable information from
   * @return array of raw classes representing arguments for the {@code type} else {@code null} if no type arguments are
   *         declared
   */
  static Class<?>[] resolveRawArgs(Class<?> type, Class<?> subType) {
    return resolveRawArguments(resolveGenericType(type, subType), subType);
  }

  /**
   * Returns an array of raw classes representing arguments for the {@code genericType} using type variable information
   * from the {@code subType}. Arguments for {@code genericType} that cannot be resolved are returned as
   * {@code Unknown.class}. If no arguments can be resolved then {@code null} is returned.
   *
   * @param genericType to resolve arguments for
   * @param subType to extract type variable information from
   * @return array of raw classes representing arguments for the {@code genericType} else {@code null} if no type
   *         arguments are declared
   */
  private static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
    Class<?>[] result = null;

    if (genericType instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) genericType;
      Type[] arguments = paramType.getActualTypeArguments();
      result = new Class[arguments.length];
      for (int i = 0; i < arguments.length; i++) {
        result[i] = resolveRawClass(arguments[i], subType);
      }

    } else if (genericType instanceof TypeVariable) {
      result = new Class[1];
      result[0] = resolveRawClass(genericType, subType);

    } else if (genericType instanceof Class) {
      TypeVariable<?>[] typeParams = ((Class<?>) genericType).getTypeParameters();
      result = new Class[typeParams.length];
      for (int i = 0; i < typeParams.length; i++) {
        result[i] = resolveRawClass(typeParams[i], subType);
      }
    }

    return result;
  }

  /**
   * Returns the generic {@code type} using type variable information from the {@code subType} else {@code null} if the
   * generic type cannot be resolved.
   *
   * @param type to resolve generic type for
   * @param subType to extract type variable information from
   * @return generic {@code type} else {@code null} if it cannot be resolved
   */
  private static Type resolveGenericType(Class<?> type, Type subType) {
    Class<?> rawType;
    if (subType instanceof ParameterizedType) {
      rawType = (Class<?>) ((ParameterizedType) subType).getRawType();
    } else {
      rawType = (Class<?>) subType;
    }

    if (type.equals(rawType)) {
      return subType;
    }

    Type result;
    if (type.isInterface()) {
      for (Type superInterface : rawType.getGenericInterfaces())
        if (superInterface != null && !superInterface.equals(Object.class))
          if ((result = resolveGenericType(type, superInterface)) != null)
            return result;
    }

    Type superClass = rawType.getGenericSuperclass();
    if (superClass != null && !superClass.equals(Object.class)) {
      if ((result = resolveGenericType(type, superClass)) != null) {
        return result;
      }
    }

    return null;
  }

  private static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
    if (genericType instanceof Class) {
      return (Class<?>) genericType;

    } else if (genericType instanceof ParameterizedType) {
      return resolveRawClass(((ParameterizedType) genericType).getRawType(), subType);

    } else if (genericType instanceof GenericArrayType) {
      GenericArrayType arrayType = (GenericArrayType) genericType;
      Class<?> component = resolveRawClass(arrayType.getGenericComponentType(), subType);
      return Array.newInstance(component, 0).getClass();

    } else if (genericType instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) genericType;
      genericType = getTypeVariableMap(subType).get(variable);
      genericType = genericType == null ? resolveBound(variable)
        : resolveRawClass(genericType, subType);
    }

    return genericType instanceof Class ? (Class<?>) genericType : Unknown.class;
  }

  private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> targetType) {

    Map<TypeVariable<?>, Type> map = new HashMap<>();

    // Populate interfaces
    populateSuperTypeArgs(targetType.getGenericInterfaces(), map);

    // Populate super classes and interfaces
    Type genericType = targetType.getGenericSuperclass();
    Class<?> type = targetType.getSuperclass();
    while (type != null && !Object.class.equals(type)) {
      if (genericType instanceof ParameterizedType) {
        populateTypeArgs((ParameterizedType) genericType, map);
      }
      populateSuperTypeArgs(type.getGenericInterfaces(), map);

      genericType = type.getGenericSuperclass();
      type = type.getSuperclass();
    }

    // Populate enclosing classes
    type = targetType;
    while (type.isMemberClass()) {
      genericType = type.getGenericSuperclass();
      if (genericType instanceof ParameterizedType) {
        populateTypeArgs((ParameterizedType) genericType, map);
      }
      type = type.getEnclosingClass();
    }

    return map;
  }

  /**
   * Populates the {@code map} with with variable/argument pairs for the given {@code types}.
   */
  private static void populateSuperTypeArgs(final Type[] types, final Map<TypeVariable<?>, Type> map) {

    for (Type type : types) {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        populateTypeArgs(parameterizedType, map);
        Type rawType = parameterizedType.getRawType();
        if (rawType instanceof Class) {
          populateSuperTypeArgs(((Class<?>) rawType).getGenericInterfaces(), map);
        }
      } else if (type instanceof Class) {
        populateSuperTypeArgs(((Class<?>) type).getGenericInterfaces(), map);
      }
    }
  }

  /**
   * Populates the {@code map} with variable/argument pairs for the given {@code type}.
   */
  private static void populateTypeArgs(ParameterizedType type, Map<TypeVariable<?>, Type> map) {
    if (type.getRawType() instanceof Class) {
      TypeVariable<?>[] typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
      Type[] typeArguments = type.getActualTypeArguments();

      if (type.getOwnerType() != null) {
        Type owner = type.getOwnerType();
        if (owner instanceof ParameterizedType) {
          populateTypeArgs((ParameterizedType) owner, map);
        }
      }

      for (int i = 0; i < typeArguments.length; i++) {
        TypeVariable<?> variable = typeVariables[i];
        Type typeArgument = typeArguments[i];

        if (typeArgument instanceof Class) {
          map.put(variable, typeArgument);
        } else if (typeArgument instanceof GenericArrayType) {
          map.put(variable, typeArgument);
        } else if (typeArgument instanceof ParameterizedType) {
          map.put(variable, typeArgument);
        } else if (typeArgument instanceof TypeVariable) {
          TypeVariable<?> typeVariableArgument = (TypeVariable<?>) typeArgument;
          Type resolvedType = map.get(typeVariableArgument);
          if (resolvedType == null)
            resolvedType = resolveBound(typeVariableArgument);
          map.put(variable, resolvedType);
        }
      }
    }
  }

  /**
   * Resolves the first bound for the {@code typeVariable}, returning {@code Unknown.class} if none can be resolved.
   */
  private static Type resolveBound(TypeVariable<?> typeVariable) {
    Type[] bounds = typeVariable.getBounds();
    if (bounds.length == 0) {
      return Unknown.class;
    }

    Type bound = bounds[0];
    if (bound instanceof TypeVariable) {
      bound = resolveBound((TypeVariable<?>) bound);
    }

    return bound == Object.class ? Unknown.class : bound;
  }

}
