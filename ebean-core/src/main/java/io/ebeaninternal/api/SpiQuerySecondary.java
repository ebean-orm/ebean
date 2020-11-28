package io.ebeaninternal.api;

import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.util.List;

/**
 * The secondary query paths for 'query joins' and 'lazy loading'.
 */
public interface SpiQuerySecondary {

  /**
   * Return a list of path/properties that are query join loaded.
   */
  List<OrmQueryProperties> getQueryJoins();

  /**
   * Return the list of path/properties that are lazy loaded.
   */
  List<OrmQueryProperties> getLazyJoins();
}
