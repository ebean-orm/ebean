package com.avaje.ebean.config;

import javax.sql.DataSource;

/**
 * For multi-tenancy via DB supply the DataSource given the tenantId.
 */
public interface TenantDataSourceProvider {

  /**
   * Return the DataSource to use for the given current tenant.
   */
  DataSource dataSource(String tenantId);

  /**
   * Shutdown all the DataSources.
   */
  void shutdown(boolean deregisterDriver);
}
