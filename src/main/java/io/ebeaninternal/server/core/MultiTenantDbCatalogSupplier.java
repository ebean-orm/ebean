package io.ebeaninternal.server.core;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantCatalogProvider;
import io.ebeaninternal.server.transaction.DataSourceSupplier;
import org.avaje.datasource.DataSourcePool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DataSource supplier that changes DB catalog based on current Tenant Id.
 */
public class MultiTenantDbCatalogSupplier implements DataSourceSupplier {

  private final DataSource dataSource;
  private final DataSource readOnlyDataSource;

  private final CatalogDataSource catalogDataSource;
  private final CatalogDataSource readOnly;

  MultiTenantDbCatalogSupplier(CurrentTenantProvider tenantProvider, DataSource dataSource, DataSource readOnlyDataSource, TenantCatalogProvider catalogProvider) {
    this.dataSource = dataSource;
    this.readOnlyDataSource = readOnlyDataSource;
    this.catalogDataSource = new CatalogDataSource(dataSource, tenantProvider, catalogProvider);
    if (readOnlyDataSource == null) {
      this.readOnly = null;
    } else {
      this.readOnly = new CatalogDataSource(readOnlyDataSource, tenantProvider, catalogProvider);
    }
  }

  @Override
  public DataSource getDataSource() {
    return catalogDataSource;
  }

  @Override
  public DataSource getReadOnlyDataSource() {
    return readOnly;
  }

  @Override
  public Connection getConnection(Object tenantId) throws SQLException {
    return catalogDataSource.getConnectionForTenant(tenantId);
  }

  @Override
  public Connection getReadOnlyConnection(Object tenantId) throws SQLException {
    return readOnly.getConnectionForTenant(tenantId);
  }

  @Override
  public void shutdown(boolean deregisterDriver) {
    if (readOnlyDataSource instanceof DataSourcePool) {
      ((DataSourcePool) readOnlyDataSource).shutdown(false);
    }
    if (dataSource instanceof DataSourcePool) {
      ((DataSourcePool) dataSource).shutdown(deregisterDriver);
    }
  }

  private class CatalogDataSource implements DataSource {

    final DataSource dataSource;
    final CurrentTenantProvider tenantProvider;
    final TenantCatalogProvider catalogProvider;

    CatalogDataSource(DataSource dataSource, CurrentTenantProvider tenantProvider, TenantCatalogProvider catalogProvider) {
      this.dataSource = dataSource;
      this.tenantProvider = tenantProvider;
      this.catalogProvider = catalogProvider;
    }

    /**
     * Returns the DB catalog for the current user Tenant Id.
     */
    private String tenantCatalog() {
      return catalogProvider.catalog(tenantProvider.currentId());
    }

    /**
     * Return the connection where tenantId is optionally provided by a lazy loading query.
     */
    Connection getConnectionForTenant(Object tenantId) throws SQLException {
      Connection connection = dataSource.getConnection();
      connection.setCatalog(catalogProvider.catalog(tenantId));
      return connection;
    }

    /**
     * Return the connection with the appropriate DB catalog set.
     */
    @Override
    public Connection getConnection() throws SQLException {

      Connection connection = dataSource.getConnection();
      connection.setCatalog(tenantCatalog());
      return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      return dataSource.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
      dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
      dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
      return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return dataSource.getParentLogger();
    }

  }
}
