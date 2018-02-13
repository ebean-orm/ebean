package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.util.JdbcClose;
import org.slf4j.Logger;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates transactions with implementations for basic mode and multi-tenancy mode.
 */
abstract class TransactionFactory {

  static final Logger TXN_LOGGER = TransactionManager.TXN_LOGGER;

  final AtomicLong counter = new AtomicLong(1000);

  final TransactionManager manager;

  TransactionFactory(TransactionManager manager) {
    this.manager = manager;
  }

  /**
   * Return a new query only transaction.
   *
   * @param tenantId The tenantId for lazy loading queries.
   */
  abstract SpiTransaction createQueryTransaction(Object tenantId);

  /**
   * Return a new transaction.
   */
  abstract SpiTransaction createTransaction(boolean explicit, int isolationLevel);

  /**
   * Set the Transaction Isolation level if required.
   */
  SpiTransaction setIsolationLevel(SpiTransaction t, boolean explicit, int isolationLevel) {

    if (isolationLevel > -1) {
      Connection connection = t.getConnection();
      try {
        connection.setTransactionIsolation(isolationLevel);
      } catch (SQLException e) {
        JdbcClose.close(connection);
        throw new PersistenceException(e);
      }
    }

    if (explicit && TXN_LOGGER.isTraceEnabled()) {
      TXN_LOGGER.trace(t.getLogPrefix() + "Begin");
    }

    return t;
  }
}
