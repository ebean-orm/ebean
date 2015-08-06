package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * MySql specific DDL.
 */
public class MySqlDdl extends PlatformDdl {

  public MySqlDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.namingConvention.maxConstraintNameLength = 64;
  }

  /**
   * Return the drop index statement.
   */
  @Override
  public String dropIndex(String indexName, String tableName) {
    return "drop index " + indexName + " on " + tableName;
  }

  /**
   * Return the drop foreign key clause.
   */
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + tableName + " drop foreign key " + fkName;
  }

}
