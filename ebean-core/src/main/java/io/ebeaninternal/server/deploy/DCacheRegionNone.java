package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.SpiCacheRegion;

final class DCacheRegionNone implements SpiCacheRegion {

  static final SpiCacheRegion INSTANCE = new DCacheRegionNone();

  @Override
  public String name() {
    return "<none>";
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void setEnabled(boolean enabled) {
    throw new IllegalStateException("Not expected");
  }
}
