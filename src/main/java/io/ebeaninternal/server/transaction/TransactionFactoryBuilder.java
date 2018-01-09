package io.ebeaninternal.server.transaction;

import io.ebean.config.CurrentTenantProvider;

/**
 * Helper to build and return the appropriate TransactionFactory.
 */
class TransactionFactoryBuilder {

  /**
   * Build and return based on multi-tenancy and read only DataSource.
   */
  static TransactionFactory build(TransactionManager manager, DataSourceSupplier dataSourceSupplier, CurrentTenantProvider tenantProvider) {

    boolean hasReadOnlyDataSource = dataSourceSupplier.getReadOnlyDataSource() != null;
    if (tenantProvider == null) {
      if (hasReadOnlyDataSource) {
        return new TransactionFactoryBasicWithRead(manager, dataSourceSupplier);
      } else {
        return new TransactionFactoryBasic(manager, dataSourceSupplier);
      }
    } else {
      if (hasReadOnlyDataSource) {
        return new TransactionFactoryTenantWithRead(manager, dataSourceSupplier, tenantProvider);
      } else {
        return new TransactionFactoryTenant(manager, dataSourceSupplier, tenantProvider);
      }
    }
  }
}
