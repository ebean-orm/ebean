package io.ebean.config;

import io.ebean.lookup.Lookups;

/**
 * Helper to find classes taking into account the context class loader.
 */
public class ClassLoadConfig {

  protected final ClassLoaderContext context;

  /**
   * Construct with the default classLoader search with context classLoader first.
   */
  public ClassLoadConfig() {
    this(null);
  }

  /**
   * Specify the classLoader to use for class detection and new instance creation.
   */
  public ClassLoadConfig(ClassLoader classLoader) {
    this.context = new ClassLoaderContext(classLoader);
  }

  /**
   * Return true if the Joda types are available and should be supported.
   */
  public boolean isJodaTimePresent() {
    return isPresent("org.joda.time.LocalDateTime");
  }

  /**
   * Return true if javax validation annotations like Size and NotNull are present.
   */
  public boolean isJavaxValidationAnnotationsPresent() {
    return isPresent("javax.validation.constraints.NotNull");
  }

  /**
   * Return true if jakarta validation annotations like Size and NotNull are present.
   */
  public boolean isJakartaValidationAnnotationsPresent() {
    return isPresent("jakarta.validation.constraints.NotNull");
  }

  /**
   * Return true if javax PostConstruct annotation is present (maybe not in java9).
   * If not we don't support PostConstruct lifecycle events.
   */
  public boolean isJavaxPostConstructPresent() {
    return isPresent("javax.annotation.PostConstruct");
  }

  /**
   * Return true if Jackson annotations like JsonIgnore are present.
   */
  public boolean isJacksonAnnotationsPresent() {
    return isPresent("com.fasterxml.jackson.annotation.JsonIgnore");
  }

  public boolean isJacksonCorePresent() {
    return isPresent("com.fasterxml.jackson.core.JsonParser");
  }

  /**
   * Return true if Jackson ObjectMapper is present.
   */
  public boolean isJacksonObjectMapperPresent() {
    return isPresent("com.fasterxml.jackson.databind.ObjectMapper");
  }

  /**
   * Return a new instance of the class using the default constructor.
   */
  public Object newInstance(String className) {
    try {
      return Lookups.newDefaultInstance(forName(className));
    } catch (Throwable e) {
      throw new IllegalArgumentException("Error constructing " + className, e);
    }
  }

  /**
   * Return true if the given class is present.
   */
  public boolean isPresent(String className) {
    try {
      forName(className);
      return true;
    } catch (Throwable ex) {
      // Class or one of its dependencies is not present...
      return false;
    }
  }

  /**
   * Load a class taking into account a context class loader (if present).
   */
  protected Class<?> forName(String name) throws ClassNotFoundException {
    return context.forName(name);
  }

  /**
   * Return the classLoader to use for service loading etc.
   */
  public ClassLoader getClassLoader() {
    return context.getClassLoader();
  }

  /**
   * Wraps the preferred, caller and context class loaders.
   */
  protected static class ClassLoaderContext {

    /**
     * Optional - if set only use this classLoader (no fallback).
     */
    protected final ClassLoader preferredLoader;
    protected final ClassLoader contextLoader;
    protected final ClassLoader callerLoader;

    ClassLoaderContext(ClassLoader preferredLoader) {
      this.preferredLoader = preferredLoader;
      this.callerLoader = DatabaseConfig.class.getClassLoader();
      this.contextLoader = contextLoader();
    }

    ClassLoader contextLoader() {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return (loader != null) ? loader : callerLoader;
    }

    Class<?> forName(String name) throws ClassNotFoundException {
      if (preferredLoader != null) {
        // only use the explicitly set classLoader
        return classForName(name, preferredLoader);
      }
      try {
        // try the context loader first
        return classForName(name, contextLoader);
      } catch (ClassNotFoundException e) {
        if (callerLoader == contextLoader) {
          throw e;
        } else {
          // fallback to the caller classLoader
          return classForName(name, callerLoader);
        }
      }
    }

    Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
      return Class.forName(name, true, classLoader);
    }

    ClassLoader getClassLoader() {
      return preferredLoader != null ? preferredLoader : contextLoader;
    }
  }
}

