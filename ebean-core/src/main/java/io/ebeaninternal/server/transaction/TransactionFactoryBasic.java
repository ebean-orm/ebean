package io.ebeaninternal.server.transaction;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transaction factory when no multi-tenancy is used.
 */
class TransactionFactoryBasic extends TransactionFactory {

  protected final DataSource dataSource;

  TransactionFactoryBasic(TransactionManager manager, DataSourceSupplier dataSourceSupplier) {
    super(manager);
    this.dataSource = dataSourceSupplier.dataSource();
  }

  @Override
  public SpiTransaction createReadOnlyTransaction(Object tenantId, boolean useMaster) {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      return new ImplicitReadOnlyTransaction(true, manager, connection);
    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  @Override
  public final SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      SpiTransaction t = manager.createTransaction(explicit, connection);
      return setIsolationLevel(t, isolationLevel);
    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
