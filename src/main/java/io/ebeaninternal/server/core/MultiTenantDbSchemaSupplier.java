package io.ebeaninternal.server.core;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantSchemaProvider;
import io.ebeaninternal.server.transaction.DataSourceSupplier;
import org.avaje.datasource.DataSourcePool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DataSource supplier that changes DB schema based on current Tenant Id.
 */
class MultiTenantDbSchemaSupplier implements DataSourceSupplier {

  private final DataSource dataSource;
  private final DataSource readOnlyDataSource;

  private final SchemaDataSource schemaDataSource;
  private final SchemaDataSource readOnly;

  MultiTenantDbSchemaSupplier(CurrentTenantProvider tenantProvider, DataSource dataSource, DataSource readOnlyDataSource, TenantSchemaProvider schemaProvider) {
    this.dataSource = dataSource;
    this.readOnlyDataSource = readOnlyDataSource;
    this.schemaDataSource = new SchemaDataSource(dataSource, schemaProvider, tenantProvider);
    if (readOnlyDataSource == null) {
      this.readOnly = null;
    } else {
      this.readOnly = new SchemaDataSource(readOnlyDataSource, schemaProvider, tenantProvider);
    }
  }

  @Override
  public DataSource getDataSource() {
    return schemaDataSource;
  }

  @Override
  public DataSource getReadOnlyDataSource() {
    return readOnly;
  }

  @Override
  public Connection getConnection(Object tenantId) throws SQLException {
    return schemaDataSource.getConnectionForTenant(tenantId);
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

  /**
   * Tenant schema aware DataSource.
   */
  private static class SchemaDataSource implements DataSource {

    private final DataSource dataSource;
    private final TenantSchemaProvider schemaProvider;
    private final CurrentTenantProvider tenantProvider;

    SchemaDataSource(DataSource dataSource, TenantSchemaProvider schemaProvider, CurrentTenantProvider tenantProvider) {
      this.dataSource = dataSource;
      this.schemaProvider = schemaProvider;
      this.tenantProvider = tenantProvider;
    }

    /**
     * Return the connection where tenantId is optionally provided by a lazy loading query.
     */
    Connection getConnectionForTenant(Object tenantId) throws SQLException {
      Connection connection = dataSource.getConnection();
      connection.setSchema(schemaProvider.schema(tenantId));
      return connection;
    }

    /**
     * Returns the DB schema for the current user Tenant Id.
     */
    private String tenantSchema() {
      return schemaProvider.schema(tenantProvider.currentId());
    }

    /**
     * Return the connection with the appropriate DB schema set.
     */
    @Override
    public Connection getConnection() throws SQLException {

      Connection connection = dataSource.getConnection();
      connection.setSchema(tenantSchema());
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
