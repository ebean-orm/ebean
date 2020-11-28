package io.ebean.core.type;

import java.sql.SQLException;

/**
 * Reads from and binds to database columns.
 */
public interface ScalarDataReader<T> {

  /**
   * Read and return the appropriate value from the dataReader.
   */
  T read(DataReader reader) throws SQLException;

}
