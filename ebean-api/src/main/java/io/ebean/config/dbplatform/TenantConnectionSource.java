package io.ebean.config.dbplatform;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Optionally implemented by the DataSource passed to a {@link SequenceIdGenerator}
 * to make sequence id allocation multi-tenant aware.
 * <p>
 * When the DataSource implements this interface the sequence generator maintains
 * a separate id buffer per tenant and obtains connections that are routed to the
 * correct tenant database (TenantMode.DB) or schema/catalog (TenantMode.SCHEMA / CATALOG).
 * <p>
 * The {@link #connectionForTenant(Object)} method takes an explicit tenantId so that
 * background pre-fetch (which runs on a separate thread without the current tenant
 * in scope) can fetch sequence values for the tenant captured at submit time.
 */
public interface TenantConnectionSource {

  /**
   * Return the current tenant id, or null when there is no current tenant scope.
   */
  Object currentTenantId();

  /**
   * Return a connection routed to the given tenant (its database, schema or catalog).
   */
  Connection connectionForTenant(Object tenantId) throws SQLException;
}
