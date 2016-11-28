package com.avaje.ebean.config;

/**
 * The mode to use for multi-tenancy.
 */
public enum TenantMode {

  /**
   * No multi-tenancy.
   */
  NONE,

  /**
   * Each Tenant has their own Database (javax.sql.DataSource)
   */
  DB,

  /**
   * Each Tenant has their own Database schema.
   */
  SCHEMA,

  /**
   * Tenants share tables but have a discriminator/partition column that partitions the data.
   */
  PARTITION
}
