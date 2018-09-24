package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;

import java.io.IOException;
import java.util.Collection;

/**
 * MySql specific DDL.
 */
public class MySqlDdl extends PlatformDdl {

  public MySqlDdl(DatabasePlatform platform) {
    super(platform);
    this.alterColumn = "modify";
    this.dropUniqueConstraint = "drop index";
    this.historyDdl = new MySqlHistoryDdl();
    this.inlineComments = true;
  }

  /**
   * Return the drop index statement.
   */
  @Override
  public String dropIndex(String indexName, String tableName) {
    return "drop index " + maxConstraintName(indexName) + " on " + tableName;
  }

  /**
   * Return the drop foreign key clause.
   */
  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + tableName + " drop foreign key " + maxConstraintName(fkName);
  }

  /**
   * It is rather complex to delete a column on MySql as there must not exist any foreign keys.
   * That's why we call a user stored procedure here
   */
  @Override
  public void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) throws IOException {

    buffer.append("CALL usp_ebean_drop_column('").append(tableName).append("', '").append(columnName).append("')").endOfStatement();
  }

  @Override
  public String alterTableDropConstraint(String tableName, String constraintName) {
    // drop constraint not supported in MySQL 5.7 and 8.0 but starting with MariaDB 10.2.1 CHECK is evaluated
    // TODO: Implement for MariaDB >= 10.2.1
    return null;
  }

  @Override
  public String alterColumnType(String tableName, String columnName, String type) {
    // can't alter itself - done in alterColumnBaseAttributes()
    return null;
  }

  @Override
  public String alterColumnNotnull(String tableName, String columnName, boolean notnull) {

    // can't alter itself - done in alterColumnBaseAttributes()
    return null;
  }

  @Override
  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {

    String suffix = DdlHelp.isDropDefault(defaultValue) ? columnDropDefault : columnSetDefault + " " + convertDefaultValue(defaultValue);

    // use alter
    return "alter table " + tableName + " alter " + columnName + " " + suffix;
  }

  @Override
  public String alterColumnBaseAttributes(AlterColumn alter) {
    if (DdlHelp.isDropDefault(alter.getDefaultValue())) {
      return null;
    }
    String tableName = alter.getTableName();
    String columnName = alter.getColumnName();
    String type = alter.getType() != null ? alter.getType() : alter.getCurrentType();
    type = convert(type, false);
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
        comment = comment.substring(0, 500);
      }
      buffer.append(String.format(" comment '%s'", comment));
    }

  }

  @Override
  public void inlineTableComment(DdlBuffer apply, String tableComment) throws IOException {
    if (tableComment.length() > 1000) {
      tableComment = tableComment.substring(0, 1000);
    }
    apply.append(" comment='").append(tableComment).append("'");
  }

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) throws IOException {
    if (DdlHelp.isDropComment(tableComment)) {
      tableComment = "";
    }
    apply.append(String.format("alter table %s comment = '%s'", tableName, tableComment)).endOfStatement();
  }

  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) throws IOException {
    // alter comment currently not supported as it requires to repeat whole column definition
  }


  /**
   * Locks all tables for triggers that have to be updated.
   */
  @Override
  public void lockTables(DdlBuffer buffer, Collection<String> tables) throws IOException {
    if (!tables.isEmpty()) {
      buffer.append("lock tables ");
      int i = 0;
      for (String table : tables) {
        if (i > 0) {
          buffer.append(", ");
        }
        buffer.append(table).append(" write");
        i++;
      }
      buffer.endOfStatement();

    }
  }

  /**
   * Unlocks all tables for triggers that have to be updated.
   */
  @Override
  public void unlockTables(DdlBuffer buffer, Collection<String> tables) throws IOException {
    buffer.append("unlock tables").endOfStatement();
  }

}
