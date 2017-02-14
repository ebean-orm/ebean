package io.ebeaninternal.server.core;

import io.ebeaninternal.server.transaction.DataSourceSupplier;
import org.avaje.datasource.DataSourcePool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple DataSource supplier when no multi-tenancy used.
 */
class SimpleDataSourceProvider implements DataSourceSupplier {

  private final DataSource dataSource;

  SimpleDataSourceProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public Connection getConnection(Object tenantId) throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void shutdown(boolean deregisterDriver) {
    if (dataSource instanceof DataSourcePool) {
      ((DataSourcePool) dataSource).shutdown(deregisterDriver);
    }
  }
}
