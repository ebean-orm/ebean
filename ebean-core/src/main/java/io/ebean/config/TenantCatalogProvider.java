package io.ebean.config;

/**
 * For multi-tenancy via DB CATALOG supply the catalog given the tenantId.
 */
@FunctionalInterface
public interface TenantCatalogProvider {

  /**
   * Return the DB catalog for the given tenantId.
   *
   * @param tenantId The current tenant id.
   * @return The DB catalog to use for the given tenant
   */
  String catalog(Object tenantId);
}
