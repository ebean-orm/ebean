package io.ebean.config;

/**
 * The mode to use for multi-tenancy.
 */
public enum TenantMode {

  /**
   * No multi-tenancy.
   */
  NONE(false, true),

  /**
   * Each Tenant has their own Database (javax.sql.DataSource)
   */
  DB(true, false),

  /**
   * Each Tenant has their own Database schema.
   */
  SCHEMA(true, false),

  /**
   * Each Tenant has their own Database but with in connection pool
   */
  CATALOG(true, false),

  /**
   * Tenants share tables but have a discriminator/partition column that partitions the data.
   */
  PARTITION(false, true),

  /**
   * Each Tenant has their own Database (javax.sql.DataSource), and there is also one master-database
   * (that holds configuration e.g.)
   */
  DB_WITH_MASTER(true, true);
  
  boolean dynamicDataSource;
  boolean ddlEnabled;

  TenantMode(boolean dynamicDataSource, boolean ddlEnabled) {
    this.dynamicDataSource = dynamicDataSource;
    this.ddlEnabled = ddlEnabled;
  }

  /**
   * Return true if the DataSource is not available on bootup.
   */
  public boolean isDynamicDataSource() {
    return dynamicDataSource;
  }
  
  /**
   * Returns true, if DDL is enabled.
   */
  public boolean isDdlEnabled() {
    return ddlEnabled;
  }
 
}
