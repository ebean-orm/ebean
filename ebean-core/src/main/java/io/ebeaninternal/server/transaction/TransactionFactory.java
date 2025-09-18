package io.ebeaninternal.server.transaction;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates transactions with implementations for basic mode and multi-tenancy mode.
 */
abstract class TransactionFactory {

  final TransactionManager manager;
  private final boolean autoCommitMode;

  TransactionFactory(TransactionManager manager) {
    this.manager = manager;
    this.autoCommitMode = manager.isAutoCommitMode();
  }

  /**
   * Return a new transaction.
   */
  SpiTransaction createTransaction(boolean explicit, Connection connection) {
    return autoCommitMode ? new JdbcAutoCommitTransaction(explicit, connection, manager) : new JdbcTransaction(explicit, connection, manager);
  }

  /**
   * Return a new query only transaction.
   *
   * @param tenantId  The tenantId for lazy loading queries.
   * @param useMaster Explicitly use the master data source rather than read only data source
   */
  abstract SpiTransaction createReadOnlyTransaction(Object tenantId, boolean useMaster);

  /**
   * Return a new transaction.
   */
  abstract SpiTransaction createTransaction(boolean explicit, int isolationLevel);

  /**
   * Set the Transaction Isolation level if required.
   */
  final SpiTransaction setIsolationLevel(SpiTransaction t, int isolationLevel) {
    if (isolationLevel > -1) {
      Connection connection = t.internalConnection();
      try {
        connection.setTransactionIsolation(isolationLevel);
      } catch (SQLException e) {
        JdbcClose.close(connection);
        throw new PersistenceException(e);
      }
    }
    return t;
  }
}
