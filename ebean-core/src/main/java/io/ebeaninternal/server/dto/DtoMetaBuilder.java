package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
class DtoMetaBuilder {

  private static final Logger log = LoggerFactory.getLogger(DtoMetaBuilder.class);

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
          final Class<?> propertyType = propertyType(method);
          properties.add(new DtoMetaProperty(typeManager, dtoType, method, name, propertyType));
        } catch (Exception e) {
          log.debug("exclude on " + dtoType + " method " + method, e);
        }
      }
    }
  }

  static Class<?> propertyType(Method method) {
    return method.getParameterTypes()[0];
  }

  static String propertyName(String methodName) {
    final String name = methodName.substring(3);
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * Include a public "setter" method - 1 argument, returns void.
   */
  static boolean includeMethod(Method method) {
    final int modifiers = method.getModifiers();
    return Modifier.isPublic(modifiers)
      && !Modifier.isStatic(modifiers)
      && Void.TYPE.equals(method.getReturnType())
      && method.getParameterTypes().length == 1
      && method.getName().startsWith("set") && method.getName().length() > 3;
  }

  private void readConstructors() {
    Constructor<?>[] constructors = dtoType.getConstructors();
    for (Constructor<?> constructor : constructors) {
      try {
        constructorList.add(new DtoMetaConstructor(typeManager, constructor, dtoType));
      } catch (Exception e) {
        // we don't want that constructor
        log.debug("exclude on " + dtoType + " constructor " + constructor, e);
      }
    }
  }

}
