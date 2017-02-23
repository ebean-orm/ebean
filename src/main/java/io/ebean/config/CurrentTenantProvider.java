package io.ebean.config;

/**
 * Provides the Tenant Id for the current request based on the current user.
 */
@FunctionalInterface
public interface CurrentTenantProvider {

  /**
   * Return the Tenant Id for the current user.
   */
  Object currentId();
}
