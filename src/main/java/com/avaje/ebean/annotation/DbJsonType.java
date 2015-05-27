package com.avaje.ebean.annotation;

/**
 * Specify the DB storage type used to store JSON content.
 */
public enum DbJsonType {

  /**
   * Store as JSON on Postgres and for other databases store as CLOB.
   */
  JSON,

  /**
   * Store as JSONB on Postgres and for other databases store as CLOB.
   */
  JSONB,

  /**
   * Store as database VARCHAR.
   */
  VARCHAR,

  /**
   * Store as database CLOB.
   */
  CLOB,

  /**
   * Store as database BLOB.
   */
  BLOB
}
