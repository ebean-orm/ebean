package com.avaje.ebeaninternal.server.transaction;

import javax.sql.DataSource;

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
   *   This should take into account multi-tenancy and the current tenantId.
   * </p>
   */
  DataSource getDataSource();

  /**
   * Shutdown the datasource de-registering the JDBC driver if requested.
   */
  void shutdown(boolean deregisterDriver);
}
