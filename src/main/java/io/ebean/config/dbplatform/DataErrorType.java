package io.ebean.config.dbplatform;

/**
 * Specific persistence error types we wish to map.
 */
public enum DataErrorType {

  /**
   * Error trying to acquire lock (e.g. failure executing select for update nowait)
   */
  AcquireLock,

  /**
   * Error with a duplicate primary or unique key.
   */
  DuplicateKey,

  /**
   * Data integrity error like an invalid foreign key.
   */
  DataIntegrity
}
