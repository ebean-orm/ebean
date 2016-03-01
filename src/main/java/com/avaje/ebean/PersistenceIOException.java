package com.avaje.ebean;

import javax.persistence.PersistenceException;

/**
 * Captures and wraps IOException's occurring during ElasticSearch processing etc.
 */
public class PersistenceIOException extends PersistenceException {

  public PersistenceIOException(String msg, Exception cause) {
    super(msg, cause);
  }

  public PersistenceIOException(Exception cause) {
    super(cause);
  }

}
