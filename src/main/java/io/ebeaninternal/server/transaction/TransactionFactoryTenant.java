package io.ebeaninternal.server.transaction;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transaction factory used with multi-tenancy.
 */
class TransactionFactoryTenant extends TransactionFactory {

  final DataSourceSupplier dataSourceSupplier;

  final CurrentTenantProvider tenantProvider;

  TransactionFactoryTenant(TransactionManager manager, DataSourceSupplier dataSourceSupplier, CurrentTenantProvider tenantProvider) {
    super(manager);
    this.dataSourceSupplier = dataSourceSupplier;
    this.tenantProvider = tenantProvider;
  }

  @Override
  public SpiTransaction createQueryTransaction(Object tenantId) {
    return create(false, tenantId);
  }

  @Override
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {

    SpiTransaction t = create(explicit, null);
    return setIsolationLevel(t, explicit, isolationLevel);
  }

  private SpiTransaction create(boolean explicit, Object tenantId) {
    Connection connection = null;
    try {
      if (tenantId == null) {
        // tenantId not set (by lazy loading) so get current tenantId
        tenantId = tenantProvider.currentId();
      }
      connection = dataSourceSupplier.getConnection(tenantId);
      SpiTransaction transaction = manager.createTransaction(explicit, connection, counter.incrementAndGet());
      transaction.setTenantId(tenantId);
      return transaction;

    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
