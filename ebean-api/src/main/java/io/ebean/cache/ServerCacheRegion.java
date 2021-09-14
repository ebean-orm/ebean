package io.ebean.cache;

/**
 * Cache region can be enabled independently.
 */
public interface ServerCacheRegion {

  /**
   * Return the region name.
   */
  String name();

  /**
   * Deprecated migrate to name().
   */
  @Deprecated
  default String getName() {
    return name();
  }

  /**
   * Return true if the cache region is enabled.
   */
  boolean isEnabled();

  /**
   * Set to true to enable the cache region.
   */
  void setEnabled(boolean enabled);

}
