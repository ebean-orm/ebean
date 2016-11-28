package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.config.CurrentTenantProvider;
import com.avaje.ebean.config.TenantSchemaProvider;
import com.avaje.ebeaninternal.server.transaction.DataSourceSupplier;
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

  private final CurrentTenantProvider tenantProvider;

  private final DataSource dataSource;

  private final TenantSchemaProvider schemaProvider;

  private final SchemaDataSource schemaDataSource;

  MultiTenantDbSchemaSupplier(CurrentTenantProvider tenantProvider, DataSource dataSource, TenantSchemaProvider schemaProvider) {
    this.tenantProvider = tenantProvider;
    this.dataSource = dataSource;
    this.schemaProvider = schemaProvider;
    this.schemaDataSource = new SchemaDataSource();
  }

  @Override
  public DataSource getDataSource() {
    return schemaDataSource;
  }

  @Override
  public void shutdown(boolean deregisterDriver) {
    if (dataSource instanceof DataSourcePool) {
      ((DataSourcePool) dataSource).shutdown(deregisterDriver);
    }
  }

  /**
   * Returns the DB schema for the current user Tenant Id.
   */
  private String tenantSchema() {
    return schemaProvider.schema(tenantProvider.currentId());
  }

  private class SchemaDataSource implements DataSource {

    SchemaDataSource() {
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
