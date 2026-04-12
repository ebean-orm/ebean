package io.ebeaninternal.server.transaction;

import org.jspecify.annotations.Nullable;

import java.sql.Connection;

/**
 * Ebean transaction used for AutoCommit true connections.
 */
final class JdbcAutoCommitTransaction extends JdbcTransaction {

  JdbcAutoCommitTransaction(boolean explicit, Connection connection, @Nullable TransactionManager manager) {
    super(true, explicit, connection, manager);
  }

  @Override
  void performRollback() {
    long offset = profileOffset();
    if (profileStream != null) {
      profileStream.addEvent(EVT_ROLLBACK, offset);
    }
  }

  @Override
  void performCommit() {
    long offset = profileOffset();
    if (profileStream != null) {
      profileStream.addEvent(EVT_COMMIT, offset);
    }
  }

}
