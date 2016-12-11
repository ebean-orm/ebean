package io.ebean;

import javax.persistence.PersistenceException;

/**
 * Captures and wraps IOException's occurring during ElasticSearch processing etc.
 */
public class PersistenceIOException extends PersistenceException {

  private static final long serialVersionUID = -7630050437148176148L;

  public PersistenceIOException(String msg, Exception cause) {
    super(msg, cause);
  }

  public PersistenceIOException(Exception cause) {
    super(cause);
  }

}
