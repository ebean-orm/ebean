package io.ebeaninternal.server.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Hold the JDBC PreparedStatement and ResultSet.
 *
 * These both must be closed properly when done.
 */
public class SpiResultSet {

  private final PreparedStatement statement;

  private final ResultSet resultSet;

  public SpiResultSet(PreparedStatement statement, ResultSet resultSet) {
    this.statement = statement;
    this.resultSet = resultSet;
  }

  public PreparedStatement getStatement() {
    return statement;
  }

  public ResultSet getResultSet() {
    return resultSet;
  }
}
