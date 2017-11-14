package io.ebeaninternal.server.persist;


import java.sql.SQLException;

/**
 * Holds the first SQLException found when executing a JDBC batch.
 */
public class BatchedSqlException extends Exception {
  private static final long serialVersionUID = -4374631080253580648L;

  private final SQLException cause;

  BatchedSqlException(String message, SQLException cause) {
    super(message, cause);
    this.cause = cause;
  }

  @Override
  public SQLException getCause() {
    return cause;
  }
}
