package io.ebeaninternal.server.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This only works for Postgres and H2 (and doesn't work for Oracle).
 * <p>
 * Uses explicit begin statement to start the transactions.
 */
public class ExplicitJdbcTransaction extends JdbcTransaction {

  public ExplicitJdbcTransaction(String id, boolean explicit, Connection connection, TransactionManager manager) {
    super(id, explicit, connection, manager);
  }

  @Override
  protected void checkAutoCommit(Connection connection) throws SQLException {
    // begin the transaction explicitly
    executeStatement("begin");
  }

  @Override
  protected void performRollback() throws SQLException {
    // Postgres needs this explicit rollback statement when used with AutoCommit=true
    executeStatement("rollback");
  }

  @Override
  protected void performCommit() throws SQLException {
    // Postgres needs this explicit commit statement when used with AutoCommit=true
    executeStatement("commit");
  }

  private void executeStatement(String statement) throws SQLException {
    try (PreparedStatement stmt = connection.prepareStatement(statement)) {
      stmt.execute();
    }
  }

}
