package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.util.JdbcClose;

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
    return create(false);
  }

  @Override
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    SpiTransaction t = create(explicit);
    return setIsolationLevel(t, explicit, isolationLevel);
  }

  private SpiTransaction create(boolean explicit) {
    Connection c = null;
    try {
      c = dataSource.getConnection();
      return manager.createTransaction(explicit, c, counter.incrementAndGet());

    } catch (PersistenceException ex) {
      JdbcClose.close(c);
      throw ex;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
