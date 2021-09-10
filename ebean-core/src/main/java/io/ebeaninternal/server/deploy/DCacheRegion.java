package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.SpiCacheRegion;

public final class DCacheRegion implements SpiCacheRegion {

  private boolean enabled = true;
  private final String name;

  public DCacheRegion(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
