package io.ebeaninternal.json;

import java.io.Serializable;

/**
 * Owner object notified when a modification is detected.
 */
public interface ModifyAwareOwner extends Serializable {

  /**
   * Return true if the value is considered dirty.
   * Note that this resets the dirty status back to clean.
   */
  boolean isMarkedDirty();

  /**
   * Marks the object as modified.
   */
  void markAsModified();

  /**
   * Reset the dirty state to clean.
   */
  void resetMarkedDirty();

}
