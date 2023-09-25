package io.ebeaninternal.server.transaction;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;

import jakarta.persistence.PersistenceException;
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
final class TransactionFactoryTenantWithRead extends TransactionFactoryTenant {

  TransactionFactoryTenantWithRead(TransactionManager manager, DataSourceSupplier dataSourceSupplier, CurrentTenantProvider tenantProvider) {
    super(manager, dataSourceSupplier, tenantProvider);
  }

  @Override
  public SpiTransaction createReadOnlyTransaction(Object tenantId, boolean useMaster) {
    Connection connection = null;
    try {
      if (tenantId == null) {
        // obtain the tenantId if the DataSource requires it
        tenantId = dataSourceSupplier.currentTenantId();
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
}
