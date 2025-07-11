package io.ebeaninternal.json;

import io.ebean.ModifyAwareType;

import java.io.Serializable;

/**
 * Detects when content has been modified and as such needs to be persisted (included in an update).
 */
public final class ModifyAwareFlag implements ModifyAwareType, Serializable {

  private static final long serialVersionUID = 1;

  private boolean markedDirty;

  @Override
  public boolean isMarkedDirty() {
    return markedDirty;
  }

  @Override
  public void setMarkedDirty(boolean markedDirty) {
    this.markedDirty = markedDirty;
  }
}
