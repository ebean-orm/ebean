package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.SpiCacheRegion;

public class DCacheRegion implements SpiCacheRegion {

  private boolean enabled = true;

  private final String name;

  public DCacheRegion(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
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
