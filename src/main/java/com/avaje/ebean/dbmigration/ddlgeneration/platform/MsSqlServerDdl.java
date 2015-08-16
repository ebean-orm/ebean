package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.dbmigration.migration.AlterColumn;

/**
 * MS SQL Server platform specific DDL.
 */
public class MsSqlServerDdl extends PlatformDdl {

  public MsSqlServerDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.identitySuffix = " identity(1,1)";
    this.foreignKeyRestrict = "";
    this.inlineUniqueOneToOne = false;
    this.columnSetDefault = "add default";
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
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns) {

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

  @Override
  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {

    if (isDropDefault(defaultValue)) {
      return "-- alter table " + tableName + " drop constraint <unknown>  -- find the appropriate constraint for default value on column " + columnName;
    } else {
      return "alter table " + tableName + " add default " + defaultValue + " for " + columnName;
    }
  }

  public String alterColumnBaseAttributes(AlterColumn alter) {

    String tableName = alter.getTableName();
    String columnName = alter.getColumnName();
    String type = alter.getType() != null ? alter.getType() : alter.getCurrentType();
    boolean notnull = (alter.isNotnull() != null) ? alter.isNotnull() : Boolean.TRUE.equals(alter.isCurrentNotnull());
    String notnullClause = notnull ? " not null" : "";

    return "alter table " + tableName + " alter column " + columnName + " " + type + notnullClause;
  }

  @Override
  public String alterColumnType(String tableName, String columnName, String type) {

    // can't alter itself - done in alterColumnBaseAttributes()
    return null;
  }

  public String alterColumnNotnull(String tableName, String columnName, boolean notnull) {

    // can't alter itself - done in alterColumnBaseAttributes()
    return null;
  }
}
