package io.ebeaninternal.server.transaction;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates transactions with implementations for basic mode and multi-tenancy mode.
 */
abstract class TransactionFactory {

  final TransactionManager manager;

  TransactionFactory(TransactionManager manager) {
    this.manager = manager;
  }

  /**
   * Return a new query only transaction.
   *
   * @param tenantId The tenantId for lazy loading queries.
   */
  abstract SpiTransaction createReadOnlyTransaction(Object tenantId);

  /**
   * Return a new transaction.
   */
  abstract SpiTransaction createTransaction(boolean explicit, int isolationLevel);

  /**
   * Set the Transaction Isolation level if required.
   */
  final SpiTransaction setIsolationLevel(SpiTransaction t, boolean explicit, int isolationLevel) {
    if (isolationLevel > -1) {
      Connection connection = t.connection();
      try {
        connection.setTransactionIsolation(isolationLevel);
      } catch (SQLException e) {
        JdbcClose.close(connection);
        throw new PersistenceException(e);
      }
    }
    if (explicit && manager.log().txn().isTrace()) {
      manager.log().txn().trace(t.getLogPrefix() + "Begin");
    }
    return t;
  }
}
