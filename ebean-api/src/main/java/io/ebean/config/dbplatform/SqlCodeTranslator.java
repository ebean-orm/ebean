package io.ebean.config.dbplatform;

import io.ebean.AcquireLockException;
import io.ebean.DataIntegrityException;
import io.ebean.DuplicateKeyException;
import io.ebean.SerializableConflictException;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Translate SQLException based on SQLState codes.
 */
public class SqlCodeTranslator implements SqlExceptionTranslator {

  private final Map<String, DataErrorType> map;

  /**
   * Create given the map of SQLState codes to error types.
   */
  public SqlCodeTranslator(Map<String, DataErrorType> map) {
    this.map = map;
  }

  /**
   * Create "No-op" implementation.
   */
  public SqlCodeTranslator() {
    this.map = Collections.emptyMap();
  }

  private DataErrorType getErrorType(SQLException e) {
    String sqlState = e.getSQLState();
    while (sqlState == null && e.getCause() instanceof SQLException) {
      e = (SQLException)e.getCause();
      sqlState = e.getSQLState();
    }
    DataErrorType errorType = map.get(sqlState);
    if (errorType == null) {
      // fall back to error code
      errorType = map.get(String.valueOf(e.getErrorCode()));
    }
    return errorType;
  }

  @Override
  public PersistenceException translate(String message, SQLException e) {

    DataErrorType errorType = getErrorType(e);
    // for DB2 we must inspect the sql exception chain to determine which
    // persistence error occurred in a batch execution. We also concatenate
    // the error messages to improve error analysis.
    SQLException chain = e.getNextException();
    if (chain != null) {
      StringBuilder sb = new StringBuilder(message);
      int i = 1;
      while (chain != null && i < 100000) { // prevents from endless loop
        sb.append("\n\t#").append(i++).append(": ").append(chain.getMessage());
        if (errorType == null) {
          errorType = getErrorType(chain);
          if (errorType != null) {
            sb.append(" (causing error)"); // mark the line, where we found a matching error code
          }
        }
        chain = chain.getNextException();
      }
      message = sb.toString();
    }

    if (errorType != null) {
      switch (errorType) {
        case AcquireLock:
          return new AcquireLockException(message, e);
        case DuplicateKey:
          return new DuplicateKeyException(message, e);
        case DataIntegrity:
          return new DataIntegrityException(message, e);
        case SerializableConflict:
          return new SerializableConflictException(message, e);
      }
    }
    // return a generic exception
    return new PersistenceException(message, e);
  }
}
