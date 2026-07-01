package io.ebeaninternal.server.transaction;

import io.ebean.config.dbplatform.TenantConnectionSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DataSource used for sequence id generation that is multi-tenant aware.
 * <p>
 * Delegates to the current tenant DataSource via the {@link DataSourceSupplier} and
 * additionally implements {@link TenantConnectionSource} so the sequence generator can
 * maintain a separate id buffer per tenant and obtain connections routed to a specific
 * tenant (needed for background pre-fetch where the current tenant is not in scope).
 */
public final class SequenceDataSource implements DataSource, TenantConnectionSource {

  private final DataSourceSupplier supplier;

  public SequenceDataSource(DataSourceSupplier supplier) {
    this.supplier = supplier;
  }

  @Override
  public Object currentTenantId() {
    return supplier.currentTenantId();
  }

  @Override
  public Connection connectionForTenant(Object tenantId) throws SQLException {
    return supplier.connection(tenantId);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return supplier.connection(supplier.currentTenantId());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return supplier.dataSource().getConnection(username, password);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return supplier.dataSource().unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return supplier.dataSource().isWrapperFor(iface);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return supplier.dataSource().getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    supplier.dataSource().setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    supplier.dataSource().setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return supplier.dataSource().getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }
}
