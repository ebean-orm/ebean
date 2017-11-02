package io.ebean.config.dbplatform;

import io.ebean.AcquireLockException;
import io.ebean.DataIntegrityException;
import io.ebean.DuplicateKeyException;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Translate SQLException based on SQLState codes.
 */
public class SqlCodeTranslator implements SqlExceptionTranslator {

  private final Map<String,DataErrorType> map;

  /**
   * Create given the map of SQLState codes to error types.
   */
  public SqlCodeTranslator(Map<String,DataErrorType> map) {
    this.map = map;
  }

  /**
   * Create "No-op" implementation.
   */
  public SqlCodeTranslator() {
    this.map = Collections.emptyMap();
  }

  @Override
  public PersistenceException translate(String message, SQLException e) {

    DataErrorType errorType = map.get(e.getSQLState());
    if (errorType == null) {
      // fall back to error code
      errorType = map.get(String.valueOf(e.getErrorCode()));
    }
    if (errorType != null) {
      switch (errorType) {
        case AcquireLock:
          return new AcquireLockException(message, e);
        case DuplicateKey:
          return new DuplicateKeyException(message, e);
        case DataIntegrity:
          return new DataIntegrityException(message, e);
      }
    }
    // return a generic exception
    return new PersistenceException(message, e);
  }
}
