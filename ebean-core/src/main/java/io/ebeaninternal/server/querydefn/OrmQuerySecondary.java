package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.api.SpiQuerySecondary;

import java.util.List;

/**
 * The secondary query paths for 'query joins' and 'lazy loading'.
 */
class OrmQuerySecondary implements SpiQuerySecondary {

  private final List<OrmQueryProperties> queryJoins;

  private final List<OrmQueryProperties> lazyJoins;

  /**
   * Construct with the 'query join' and 'lazy join' path properties.
   */
  OrmQuerySecondary(List<OrmQueryProperties> queryJoins, List<OrmQueryProperties> lazyJoins) {
    this.queryJoins = queryJoins;
    this.lazyJoins = lazyJoins;
  }

  /**
   * Return a list of path/properties that are query join loaded.
   */
  @Override
  public List<OrmQueryProperties> getQueryJoins() {
    return queryJoins;
  }

  /**
   * Return the list of path/properties that are lazy loaded.
   */
  @Override
  public List<OrmQueryProperties> getLazyJoins() {
    return lazyJoins;
  }
}
