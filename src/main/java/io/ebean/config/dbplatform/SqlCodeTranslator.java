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
    if (errorType != null) {
      switch (errorType) {
        case AcquireLock:
          return new AcquireLockException(message, e);
        case DataIntegrity:
          // workaround for new sqlserver & mysql that has the same SQLState for DataIntegrity & DuplicateKey
          String cause = null;
          if (e.getCause() != null) {
            cause = e.getCause().getMessage();
          } else {
            cause = e.getMessage();
          }
          if (cause != null 
              && (cause.contains(" duplicate key ") // SqlServer
                  || cause.startsWith("Duplicate entry ") // MySql
                  )) { 
            return new DuplicateKeyException(message, e);
          } else {
            return new DataIntegrityException(message, e);
          }
        case DuplicateKey:
          return new DuplicateKeyException(message, e);
      }
    }
    // return a generic exception
    return new PersistenceException(message, e);
  }
}
