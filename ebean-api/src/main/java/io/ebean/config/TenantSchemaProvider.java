package io.ebean.config;

/**
 * For multi-tenancy via DB SCHEMA supply the schema given the tenantId.
 */
@FunctionalInterface
public interface TenantSchemaProvider {

  /**
   * Return the DB schema for the given tenantId.
   *
   * @param tenantId The current tenant id.
   * @return The DB schema to use for the given tenant
   */
  String schema(Object tenantId);
}
