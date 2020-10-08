package io.ebeaninternal.server.persist;

/**
 * Save many that can be queued up to execute after the associated
 * bean has been actually been persisted.
 */
public interface SaveMany {

  /**
   * Save the many property (after the associated bean persist).
   */
  void saveBatch();
}
