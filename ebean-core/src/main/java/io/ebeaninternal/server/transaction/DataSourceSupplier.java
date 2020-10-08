package io.ebeaninternal.server.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Supply the DataSource to the transaction manager.
 * <p>
 * Implementations can support multi-tenancy via DB or SCHEMA.
 * </p>
 */
public interface DataSourceSupplier {

  /**
   * Return the DataSource to use for the current request.
   * <p>
   * This should take into account multi-tenancy and the current tenantId.
   * </p>
   */
  DataSource getDataSource();

  /**
   * Return the read only DataSource to use for the current request.
   * <p>
   * This can return null meaning that no read only DataSource (with autoCommit)
   * is available for use so normal transactions with explicit commit should be used.
   * </p>
   */
  DataSource getReadOnlyDataSource();

  /**
   * Return a connection from the DataSource taking into account a tenantId for multi-tenant lazy loading.
   *
   * @param tenantId Most often null but well supplied indicates a multi-tenant lazy loading query
   * @return the connection to use
   */
  Connection getConnection(Object tenantId) throws SQLException;

  /**
   * Return a connection from the read only DataSource taking into account a tenantId for multi-tenant lazy loading.
   *
   * @param tenantId Most often null but well supplied indicates a multi-tenant lazy loading query
   * @return the connection to use
   */
  Connection getReadOnlyConnection(Object tenantId) throws SQLException;

  /**
   * Shutdown the datasource de-registering the JDBC driver if requested.
   */
  void shutdown(boolean deregisterDriver);

}
