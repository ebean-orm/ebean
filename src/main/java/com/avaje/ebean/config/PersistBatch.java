package com.avaje.ebean.config;

/**
 * Defines the mode for JDBC batch processing.
 * <p>
 * Used both at a per transaction basis and per request basis.
 * </p>
 *
 * @see com.avaje.ebean.config.ServerConfig#setPersistBatch(PersistBatch)
 * @see com.avaje.ebean.config.ServerConfig#setPersistBatchOnCascade(PersistBatch)
 *
 * @see com.avaje.ebean.Transaction#setBatch(PersistBatch)
 * @see com.avaje.ebean.Transaction#setBatchOnCascade(PersistBatch)
 */
public enum PersistBatch {

  /**
   * Do not use JDBC Batch mode.
   */
  NONE(false),

  /**
   * Use JDBC Batch mode on Inserts.
   */
  INSERT(true),

  /**
   * Use JDBC Batch mode on Inserts, Updates and Deletes.
   */
  ALL(true),

  /**
   * You should not use this value explicitly. It should only used on the Transactional annotation
   * to indicate that the value should inherit from the ServerConfig setting.
   */
  INHERIT(false);


  boolean forInsert;

  PersistBatch(boolean forInsert) {
    this.forInsert = forInsert;
  }

  /**
   * Return true if persist cascade should use JDBC batch for inserts.
   */
  public boolean forInsert() {
    return forInsert;
  }

}
