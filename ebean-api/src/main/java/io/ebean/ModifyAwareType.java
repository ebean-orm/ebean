package io.ebean;

/**
 * Owner object notified when a modification is detected.
 */
public interface ModifyAwareType {

  /**
   * Return true if the value is considered dirty.
   * Note that this resets the dirty status back to clean.
   */
  boolean isMarkedDirty();

  /**
   * Marks the object as modified.
   */
  void setMarkedDirty(boolean markedDirty);

  default Object freeze() {
    return this; // throw new UnsupportedOperationException();
  }
}
