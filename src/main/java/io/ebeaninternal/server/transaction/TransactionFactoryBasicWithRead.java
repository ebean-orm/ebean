package io.ebeaninternal.server.transaction;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Aware of read only autoCommit based DataSource.
 * <p>
 * This means for implicit query only transactions we can:
 * - Use the read only DataSource
 * - Skip explicit commit (as we use AutoCommit instead)
 * </p>
 */
class TransactionFactoryBasicWithRead extends TransactionFactoryBasic {

  private final DataSource readOnlyDataSource;

  TransactionFactoryBasicWithRead(TransactionManager manager, DataSourceSupplier dataSourceSupplier) {
    super(manager, dataSourceSupplier);
    this.readOnlyDataSource = dataSourceSupplier.getReadOnlyDataSource();
  }

  @Override
  public SpiTransaction createQueryTransaction(Object tenantId) {

    Connection connection = null;
    try {
      connection = readOnlyDataSource.getConnection();
      return new ImplicitReadOnlyTransaction(manager, connection);

    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }
}
