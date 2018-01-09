package io.ebeaninternal.server.transaction;

import java.sql.Connection;

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
  protected void checkAutoCommit(Connection connection) {
    // do nothing as autoCommit
  }

  @Override
  protected void performRollback() {
    // do nothing as autoCommit
  }

  @Override
  protected void performCommit() {
    // do nothing as autoCommit
  }

}
