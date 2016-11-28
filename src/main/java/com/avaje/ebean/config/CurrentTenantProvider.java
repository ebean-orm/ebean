package com.avaje.ebean.config;

/**
 * Provides the Tenant Id for the current request based on the current user.
 */
public interface CurrentTenantProvider {

  /**
   * Return the Tenant Id for the current user.
   */
  String currentId();
}
