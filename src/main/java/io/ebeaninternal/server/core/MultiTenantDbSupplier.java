package io.ebeaninternal.server.core;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebeaninternal.server.transaction.DataSourceSupplier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DataSource supplier based on DataSource per Tenant.
 */
class MultiTenantDbSupplier implements DataSourceSupplier {

  private final CurrentTenantProvider tenantProvider;

  private final TenantDataSourceProvider dataSourceProvider;

  MultiTenantDbSupplier(CurrentTenantProvider tenantProvider, TenantDataSourceProvider dataSourceProvider) {
    this.tenantProvider = tenantProvider;
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  public DataSource getReadOnlyDataSource() {
    // read only datasource not supported with DB per tenant at this stage
    return null;
  }

  @Override
  public DataSource getDataSource() {
    return dataSourceProvider.dataSource(tenantProvider.currentId());
  }

  @Override
  public Connection getConnection(Object tenantId) throws SQLException {
    return dataSourceProvider.dataSource(tenantId).getConnection();
  }

  @Override
  public Connection getReadOnlyConnection(Object tenantId) throws SQLException {
    throw new SQLException("Not currently supported");
  }

  @Override
  public void shutdown(boolean deregisterDriver) {
    dataSourceProvider.shutdown(deregisterDriver);
  }
}
