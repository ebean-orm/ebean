package io.ebeaninternal.api;


/**
 * Helper to find classes taking into account the context class loader.
 */
public class ClassUtil {

  /**
   * Return a new instance of the class using the default constructor.
   */
  public static Object newInstance(String className) {

    try {
      Class<?> cls = forName(className);
      return cls.newInstance();
    } catch (Exception e) {
      String msg = "Error constructing " + className;
      throw new IllegalArgumentException(msg, e);
    }
  }

  /**
   * Load a class taking into account a context class loader (if present).
   */
  public static Class<?> forName(String name) throws ClassNotFoundException {
    return new ClassLoadContext().forName(name);
  }


  /**
   * Helper to wrap the context and caller classLoaders (to use/try both).
   */
  static class ClassLoadContext {

    private final ClassLoader contextLoader;

    private final ClassLoader callerLoader;

    ClassLoadContext() {
      this.callerLoader = ClassUtil.class.getClassLoader();
      this.contextLoader = contextLoader();
    }

    ClassLoader contextLoader() {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return (loader != null) ? loader : callerLoader;
    }

    public Class<?> forName(String name) throws ClassNotFoundException {

      try {
        return Class.forName(name, true, contextLoader);
      } catch (ClassNotFoundException e) {
        if (callerLoader == contextLoader) {
          throw e;
        } else {
          return Class.forName(name, true, callerLoader);
        }
      }
    }

  }
}

