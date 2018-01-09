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

  private final DataSource readOnlyDataSource;

  SimpleDataSourceProvider(DataSource dataSource, DataSource readOnlyDataSource) {
    this.dataSource = dataSource;
    this.readOnlyDataSource = readOnlyDataSource;
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public DataSource getReadOnlyDataSource() {
    return readOnlyDataSource;
  }

  @Override
  public Connection getConnection(Object tenantId) throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public Connection getReadOnlyConnection(Object tenantId) throws SQLException {
    return readOnlyDataSource.getConnection();
  }

  @Override
  public void shutdown(boolean deregisterDriver) {
    if (readOnlyDataSource instanceof DataSourcePool){
      ((DataSourcePool) readOnlyDataSource).shutdown(false);
    }
    if (dataSource instanceof DataSourcePool){
      ((DataSourcePool) dataSource).shutdown(deregisterDriver);
    }
  }
}
