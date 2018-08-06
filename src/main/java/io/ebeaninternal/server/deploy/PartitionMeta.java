package io.ebeaninternal.server.deploy;

import io.ebean.annotation.PartitionMode;

public class PartitionMeta {

  private final PartitionMode mode;

  private final String property;

  public PartitionMeta(PartitionMode mode, String property) {
    this.mode = mode;
    this.property = property;
  }

  public PartitionMode getMode() {
    return mode;
  }

  public String getProperty() {
    return property;
  }
}
