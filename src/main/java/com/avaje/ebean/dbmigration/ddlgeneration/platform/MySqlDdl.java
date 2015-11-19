package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.util.StringHelper;

import java.io.IOException;

/**
 * MySql specific DDL.
 */
public class MySqlDdl extends PlatformDdl {

  public MySqlDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.alterColumn =  "modify";
    this.dropUniqueConstraint = "drop index";
    this.historyDdl = new MySqlHistoryDdl();
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
  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + tableName + " drop foreign key " + fkName;
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

  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {

    String suffix = isDropDefault(defaultValue) ? columnDropDefault : columnSetDefault + " " + defaultValue;

    // use alter
    return "alter table " + tableName + " alter " + columnName + " " + suffix;
  }

  public String alterColumnBaseAttributes(AlterColumn alter) {

    String tableName = alter.getTableName();
    String columnName = alter.getColumnName();
    String type = alter.getType() != null ? alter.getType() : alter.getCurrentType();
    boolean notnull = (alter.isNotnull() != null) ? alter.isNotnull() : Boolean.TRUE.equals(alter.isCurrentNotnull());
    String notnullClause = notnull ? " not null" : "";

    // use modify
    return "alter table " + tableName + " modify " + columnName + " " + type + notnullClause;
  }

  @Override
  protected void writeColumnDefinition(DdlBuffer buffer, Column column, boolean useIdentity) throws IOException {
    super.writeColumnDefinition(buffer, column, useIdentity);
    String comment = column.getComment();
    if (!StringHelper.isNull(comment)) {
      // in mysql 5.5 column comment save in information_schema.COLUMNS.COLUMN_COMMENT(VARCHAR 1024)
      if (comment.length() > 500) {
        comment = comment.substring(0,500);
      }
      buffer.append(String.format(" COMMENT '%s'", comment));
    }

  }
}
