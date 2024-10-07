package io.ebeaninternal.server.dto;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.type.TypeManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Build the DtoMeta for a bean.
 * <p>
 * Use TypeManager to map bean property types to ScalarTypes.
 */
final class DtoMetaBuilder {

  private final TypeManager typeManager;
  private final Class<?> dtoType;
  private final List<DtoMetaProperty> properties = new ArrayList<>();
  private final Map<Integer, DtoMetaConstructor> constructorMap = new HashMap<>();
  private final Set<Class<?>> annotationFilter = new HashSet<>();

  DtoMetaBuilder(Class<?> dtoType, TypeManager typeManager) {
    this.dtoType = dtoType;
    this.typeManager = typeManager;
    annotationFilter.add(DbJson.class);
    annotationFilter.add(DbJsonB.class);
    if (typeManager.jsonMarkerAnnotation() != null) {
      annotationFilter.add(typeManager.jsonMarkerAnnotation());
    }
  }

  DtoMeta build() {
    readConstructors();
    readProperties();
    return new DtoMeta(dtoType, constructorMap.values(), properties);
  }

  private void readProperties() {
    for (Method method : dtoType.getMethods()) {
      if (includeMethod(method)) {
        try {
          final String name = propertyName(method.getName());
          properties.add(new DtoMetaProperty(typeManager, dtoType, method, name, annotationFilter));
        } catch (Exception e) {
          CoreLog.log.log(DEBUG, "exclude on " + dtoType + " method " + method, e);
        }
      }
    }
  }

  static String propertyName(String methodName) {
    if (isTraditionalSetterMethod(methodName)) {
      final String name = methodName.substring(3);
      return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    } else {
      // accessor style setter method
      return methodName;
    }
  }

  private static boolean isTraditionalSetterMethod(String methodName) {
    return methodName.startsWith("set") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3));
  }

  /**
   * Include a public "setter" method - 1 argument, returns void.
   */
  static boolean includeMethod(Method method) {
    String name = method.getName();
    final int modifiers = method.getModifiers();
    return Modifier.isPublic(modifiers)
      && !Modifier.isStatic(modifiers)
      && method.getParameterTypes().length == 1
      && (!name.equals("wait") && !name.equals("equals"));
  }

  private void readConstructors() {
    final Set<Integer> removal = new HashSet<>();
    for (Constructor<?> constructor : dtoType.getConstructors()) {
      try {
        final var meta = new DtoMetaConstructor(typeManager, constructor, dtoType);
        final var conflicting = constructorMap.put(meta.argCount(), meta);
        if (conflicting != null) {
          removal.add(meta.argCount());
        }
      } catch (Exception e) {
        // we don't want that constructor
        CoreLog.log.log(DEBUG, "exclude on " + dtoType + " constructor " + constructor, e);
      }
    }
    // remove the constructors that conflicted by argument count
    for (Integer key : removal) {
      constructorMap.remove(key);
    }
  }

}
