package io.ebean.config;

import javax.sql.DataSource;

/**
 * For multi-tenancy via DB supply the DataSource given the tenantId.
 */
@FunctionalInterface
public interface TenantDataSourceProvider {

  /**
   * Return the DataSource to use for the given current tenant.
   */
  DataSource dataSource(Object tenantId);

  /**
   * Shutdown all the DataSources.
   */
  default void shutdown(boolean deregisterDriver) {}
}
