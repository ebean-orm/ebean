package io.ebeaninternal.server.type;

import java.sql.SQLException;

/**
 * Reads from and binds to database columns.
 */
public interface ScalarDataReader<T> {

  /**
   * Read and return the appropriate value from the dataReader.
   */
  T read(DataReader dataReader) throws SQLException;

  /**
   * Ignore typically by moving the index position.
   */
  void loadIgnore(DataReader dataReader);

  /**
   * Bind the value to the underlying preparedStatement.
   */
  void bind(DataBind b, T value) throws SQLException;

}
