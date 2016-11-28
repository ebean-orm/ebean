package com.avaje.ebean.config;

/**
 * For multi-tenancy via DB SCHEMA supply the schema given the tenantId.
 */
public interface TenantSchemaProvider {

  /**
   * Return the DB schema for the given tenantId.
   *
   * @param tenantId The current tenant id.
   * @return The DB schema to use for the given tenant
   */
  String schema(String tenantId);
}
