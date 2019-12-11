package io.ebean.config;

import java.util.List;

/**
 * Loads and returns entity classes to register with Ebean databases.
 */
public interface ModuleInfoLoader {

  /**
   * Return the entity classes to register with the default DB.
   */
  List<Class<?>> entityClasses();

  /**
   * Return entity classes to register for a named DB (not default DB).
   */
  List<Class<?>> entityClassesFor(String dbName);
}
