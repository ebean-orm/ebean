package com.avaje.ebeaninternal.server.type;

/**
 * Owner object notified when a modification is detected.
 */
public interface ModifyAwareOwner {

  /**
   * Return true if the value is considered dirty.
   */
  public boolean isMarkedDirty();
  
  /**
   * Marks the object as modified.
   */
  public void markAsModified();
}
