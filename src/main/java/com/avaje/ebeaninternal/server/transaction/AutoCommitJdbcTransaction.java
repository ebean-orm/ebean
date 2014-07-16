package com.avaje.ebeaninternal.server.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * AutoCommit friendly Transaction.
 * <p>
 * Skips actual commit and rollback as these are performed automatically.
 */
public class AutoCommitJdbcTransaction extends JdbcTransaction {

  public AutoCommitJdbcTransaction(String id, boolean explicit, Connection connection, TransactionManager manager) {
    super(id, explicit, connection, manager);
  }

  @Override
  protected void performRollback() throws SQLException {
    // do nothing as autoCommit
  }

  @Override
  protected void performCommit() throws SQLException {
    // do nothing as autoCommit
  }

}
