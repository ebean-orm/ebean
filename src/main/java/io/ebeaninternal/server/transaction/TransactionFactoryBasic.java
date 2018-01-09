package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.util.JdbcClose;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transaction factory when no multi-tenancy is used.
 */
class TransactionFactoryBasic extends TransactionFactory {

  final DataSourceSupplier dataSourceSupplier;

  private final DataSource dataSource;

  TransactionFactoryBasic(TransactionManager manager, DataSourceSupplier dataSourceSupplier) {
    super(manager);
    this.dataSourceSupplier = dataSourceSupplier;
    this.dataSource = dataSourceSupplier.getDataSource();
  }

  @Override
  public SpiTransaction createQueryTransaction(Object tenantId) {

    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      return create(0, false, connection);

    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  @Override
  public SpiTransaction createTransaction(int profileId, boolean explicit, int isolationLevel) {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      SpiTransaction t = create(profileId, explicit, connection);
      return setIsolationLevel(t, explicit, isolationLevel);
    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  private SpiTransaction create(int profileId, boolean explicit, Connection c) {
    return manager.createTransaction(profileId, explicit, c, counter.incrementAndGet());
  }

}
