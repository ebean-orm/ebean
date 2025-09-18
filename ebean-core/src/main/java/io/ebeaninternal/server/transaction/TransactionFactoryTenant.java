package io.ebeaninternal.server.transaction;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import jakarta.persistence.PersistenceException;
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
  public SpiTransaction createReadOnlyTransaction(Object tenantId, boolean useMaster) {
    Connection connection = null;
    try {
      if (tenantId == null) {
        tenantId = tenantProvider.currentId();
      }
      connection = dataSourceSupplier.readOnlyConnection(tenantId, useMaster);
      return new ImplicitReadOnlyTransaction(manager, connection, tenantId);

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
      Object tenantId = tenantProvider.currentId();
      connection = dataSourceSupplier.connection(tenantId);
      SpiTransaction transaction = createTransaction(explicit, connection);
      transaction.setTenantId(tenantId);
      return setIsolationLevel(transaction, isolationLevel);

    } catch (PersistenceException ex) {
      JdbcClose.close(connection);
      throw ex;

    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

}
