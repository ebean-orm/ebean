package io.ebeaninternal.server.type;

import io.ebean.ModifyAwareType;

/**
 * Check dirty state of json value which might be modify aware.
 */
class CheckMarkedDirty {

  /**
   * Return true if the value should be considered dirty (and included in an update).
   */
  static boolean isDirty(Object value) {
    if (value instanceof ModifyAwareType) {
      ModifyAwareType modifyAware = (ModifyAwareType) value;
      if (modifyAware.isMarkedDirty()) {
        // reset the dirty state (consider not dirty after update)
        modifyAware.setMarkedDirty(false);
        return true;
      } else {
        return false;
      }
    }
    return true;
  }
}
