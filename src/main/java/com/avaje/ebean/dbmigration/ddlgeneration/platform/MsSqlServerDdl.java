package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * MS SQL Server platform specific DDL.
 */
public class MsSqlServerDdl extends PlatformDdl {

  public MsSqlServerDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.identitySuffix = " identity(1,1)";
    this.foreignKeyRestrict = "";
    this.inlineUniqueOneToOne = false;
    this.namingConvention.maxConstraintNameLength = 62; //Actually 128
  }

  @Override
  public String dropTable(String tableName) {
    return "IF OBJECT_ID('" + tableName + "', 'U') IS NOT NULL drop table " + tableName;
  }

  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "IF OBJECT_ID('" + fkName + "', 'F') IS NOT NULL " + super.alterTableDropForeignKey(tableName, fkName);
  }

  /**
   * MsSqlServer specific null handling on unique constraints.
   */
  @Override
  public String createExternalUniqueForOneToOne(String uqName, String tableName, String[] columns) {

    // issues#233
    String start = "create unique nonclustered index " + uqName + " on " + tableName+ "(";
    StringBuilder sb = new StringBuilder(start);

    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns[i]);
    }
    sb.append(") where");
    for (int i = 0; i < columns.length; i++) {
      sb.append(" ").append(columns[i]).append(" is not null");
    }
    return sb.toString();
  }

}
