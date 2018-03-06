package io.ebeaninternal.server.transaction;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transaction factory when no multi-tenancy is used.
 */
class TransactionFactoryBasic extends TransactionFactory {

  private final DataSource dataSource;

  TransactionFactoryBasic(TransactionManager manager, DataSourceSupplier dataSourceSupplier) {
    super(manager);
    this.dataSource = dataSourceSupplier.getDataSource();
  }

  @Override
  public SpiTransaction createQueryTransaction(Object tenantId) {

    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      return create(false, connection);

    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  @Override
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      SpiTransaction t = create(explicit, connection);
      return setIsolationLevel(t, explicit, isolationLevel);
    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  private SpiTransaction create(boolean explicit, Connection c) {
    return manager.createTransaction(explicit, c, counter.incrementAndGet());
  }

}
