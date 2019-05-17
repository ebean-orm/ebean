package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.SpiCacheControl;
import io.ebeaninternal.api.SpiCacheRegion;

/**
 * Default implementation of SpiCacheControl.
 */
class DCacheControl implements SpiCacheControl {

  private final SpiCacheRegion region;
  private final boolean bean;
  private final boolean naturalKey;
  private final boolean query;

  DCacheControl(SpiCacheRegion region, boolean bean, boolean naturalKey, boolean query) {
    this.region = region;
    this.bean = bean;
    this.naturalKey = naturalKey;
    this.query = query;
  }

  @Override
  public boolean isCaching() {
    return (bean || query) && region.isEnabled();
  }

  @Override
  public boolean isBeanCaching() {
    return bean && region.isEnabled();
  }

  @Override
  public boolean isNaturalKeyCaching() {
    return naturalKey && region.isEnabled();
  }

  @Override
  public boolean isQueryCaching() {
    return query && region.isEnabled();
  }
}
