package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.SpiCacheControl;

/**
 * Cache control used when no caching is on the bean type.
 */
class DCacheControlNone implements SpiCacheControl {

  static final SpiCacheControl INSTANCE = new DCacheControlNone();

  @Override
  public boolean isCaching() {
    return false;
  }

  @Override
  public boolean isBeanCaching() {
    return false;
  }

  @Override
  public boolean isNaturalKeyCaching() {
    return false;
  }

  @Override
  public boolean isQueryCaching() {
    return false;
  }
}
