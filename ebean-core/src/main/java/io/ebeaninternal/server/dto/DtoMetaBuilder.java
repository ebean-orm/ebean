package io.ebeaninternal.server.dto;

import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.type.TypeManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Build the DtoMeta for a bean.
 * <p>
 * Use TypeManager to map bean property types to ScalarTypes.
 */
final class DtoMetaBuilder {

  private final TypeManager typeManager;
  private final Class<?> dtoType;
  private final List<DtoMetaProperty> properties = new ArrayList<>();
  private final List<DtoMetaConstructor> constructorList = new ArrayList<>();

  DtoMetaBuilder(Class<?> dtoType, TypeManager typeManager) {
    this.dtoType = dtoType;
    this.typeManager = typeManager;
  }

  DtoMeta build() {
    readConstructors();
    readProperties();
    return new DtoMeta(dtoType, constructorList, properties);
  }

  private void readProperties() {
    for (Method method : dtoType.getMethods()) {
      if (includeMethod(method)) {
        try {
          final String name = propertyName(method.getName());
          properties.add(new DtoMetaProperty(typeManager, dtoType, method, name));
        } catch (Exception e) {
          CoreLog.log.debug("exclude on " + dtoType + " method " + method, e);
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
    for (Constructor<?> constructor : dtoType.getConstructors()) {
      try {
        constructorList.add(new DtoMetaConstructor(typeManager, constructor, dtoType));
      } catch (Exception e) {
        // we don't want that constructor
        CoreLog.log.debug("exclude on " + dtoType + " constructor " + constructor, e);
      }
    }
  }

}
