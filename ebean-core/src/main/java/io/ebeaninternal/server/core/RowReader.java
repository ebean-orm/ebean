package io.ebeaninternal.server.core;

import java.sql.SQLException;

/**
 * Read a row building a result for that row.
 */
public interface RowReader<T> {

  /**
   * Build and return a result for a row.
   */
  T read() throws SQLException;
}
