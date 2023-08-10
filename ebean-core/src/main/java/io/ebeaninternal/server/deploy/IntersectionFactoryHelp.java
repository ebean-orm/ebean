package io.ebeaninternal.server.deploy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Helper class to construct intersection beans.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class IntersectionFactoryHelp {
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private final MethodHandle handle;

  public IntersectionFactoryHelp(Class clazz, Class leftSide, Class rightSide, String factoryMethod) {
    try {
      if (factoryMethod.isEmpty()) {
        Constructor ctor = findCtor(clazz, leftSide, rightSide);
        handle = LOOKUP.findConstructor(clazz, MethodType.methodType(void.class, ctor.getParameterTypes()));
      } else {
        Method method = findMethod(clazz, factoryMethod, leftSide, rightSide);
        handle = LOOKUP.findStatic(clazz, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
      }
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException("The factory" + clazz.getName()
        + " must define a public constructor or static factory method that accepts (" + leftSide.getName() + ", " + rightSide.getName() + ")", e);
    }
  }

  private Constructor findCtor(Class clazz, Class leftSide, Class rightSide) throws NoSuchMethodException {
    for (Constructor constructor : clazz.getConstructors()) {
      Class[] types = constructor.getParameterTypes();
      if (types.length == 2) {
        // we are only interested in ctors with 2 arguments
        if (types[0].isAssignableFrom(leftSide) && types[1].isAssignableFrom(rightSide)) {
          return constructor;
        }
      }
    }
    throw new NoSuchMethodException("Could not find valid constructor");
  }

  private Method findMethod(Class clazz, String methodName, Class leftSide, Class rightSide) throws NoSuchMethodException {
    for (Method method : clazz.getMethods()) {
      Class[] types = method.getParameterTypes();
      if (types.length == 2 && method.getName().equals(methodName)) {
        // we are only interested in ctors with 2 arguments
        if (types[0].isAssignableFrom(leftSide) && types[1].isAssignableFrom(rightSide)) {
          return method;
        }
      }
    }
    throw new NoSuchMethodException("Could not find valid method");
  }

  public Object invoke(Object left, Object right) {
    try {
      return handle.invoke(left, right);
    } catch (Throwable e) {
      throw new RuntimeException("Unexpected error creating Intersection bean", e);
    }
  }
}
