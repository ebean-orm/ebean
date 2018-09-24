package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.server.persist.platform.MultiValueBind;

import java.io.IOException;

/**
 * MS SQL Server platform specific DDL.
 */
public class SqlServerDdl extends PlatformDdl {

  public SqlServerDdl(DatabasePlatform platform) {
    super(platform);
    this.identitySuffix = " identity(1,1)";
    this.alterTableIfExists = "";
    this.addColumn = "add";
    this.inlineUniqueWhenNullable = false;
    this.columnSetDefault = "add default";
    this.dropConstraintIfExists = "drop constraint";
    this.historyDdl = new SqlServerHistoryDdl();
  }

  @Override
  protected void appendForeignKeyMode(StringBuilder buffer, String onMode, ConstraintMode mode) {
    if (mode != ConstraintMode.RESTRICT) {
      super.appendForeignKeyMode(buffer, onMode, mode);
    }
  }

  @Override
  public String dropTable(String tableName) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("IF OBJECT_ID('");
    buffer.append(tableName);
    buffer.append("', 'U') IS NOT NULL drop table ");
    buffer.append(tableName);
    return buffer.toString();
  }

  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    int pos = tableName.lastIndexOf('.');
    String objectId = maxConstraintName(fkName);
    if (pos != -1) {
      objectId = tableName.substring(0, pos + 1) + fkName;
    }
    return "IF OBJECT_ID('" + objectId + "', 'F') IS NOT NULL " + super.alterTableDropForeignKey(tableName, fkName);
  }

  @Override
  public String dropSequence(String sequenceName) {
    return "IF OBJECT_ID('" + sequenceName + "', 'SO') IS NOT NULL drop sequence " + sequenceName;
  }

  @Override
  public String dropIndex(String indexName, String tableName) {
    return "IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('" + tableName + "','U') AND name = '"
        + maxConstraintName(indexName) + "') drop index " + maxConstraintName(indexName) + " ON " + tableName;
  }
  /**
   * MsSqlServer specific null handling on unique constraints.
   */
  @Override
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, String[] nullableColumns) {
    if (nullableColumns == null || nullableColumns.length == 0) {
      return super.alterTableAddUniqueConstraint(tableName, uqName, columns, nullableColumns);
    }
    if (uqName == null) {
      throw new NullPointerException();
    }
    // issues#233
    String start = "create unique nonclustered index " + uqName + " on " + tableName + "(";
    StringBuilder sb = new StringBuilder(start);

    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns[i]);
    }
    sb.append(") where");
    String sep = " ";
    for (String column : nullableColumns) {
      sb.append(sep).append(column).append(" is not null");
      sep = " and ";
    }
    return sb.toString();
  }

  @Override
  public String alterTableDropConstraint(String tableName, String constraintName) {
    StringBuilder sb = new StringBuilder();
    sb.append("IF (OBJECT_ID('").append(constraintName).append("', 'C') IS NOT NULL) ");
    sb.append(super.alterTableDropConstraint(tableName, constraintName));
    return sb.toString();
  }
  /**
   * Drop a unique constraint from the table (Sometimes this is an index).
   */
  @Override
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    StringBuilder sb = new StringBuilder();
    sb.append("IF (OBJECT_ID('").append(maxConstraintName(uniqueConstraintName)).append("', 'UQ') IS NOT NULL) ");
    sb.append(super.alterTableDropUniqueConstraint(tableName, uniqueConstraintName)).append(";\n");
    sb.append(dropIndex(uniqueConstraintName, tableName));
    return sb.toString();
  }
  /**
   * Generate and return the create sequence DDL.
   */
  @Override
  public String createSequence(String sequenceName, int initialValue, int allocationSize) {

    StringBuilder sb = new StringBuilder("create sequence ");
    sb.append(sequenceName);
    sb.append(" as bigint ");
    if (initialValue > 1) {
      sb.append(" start with ").append(initialValue);
    } else {
      sb.append(" start with 1 ");
    }
    if (allocationSize > 1) {
      sb.append(" increment by ").append(allocationSize);
    }
    sb.append(";");
    return sb.toString();
  }

  @Override
  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {
    // Unfortunately, the SqlServer creates default values with a random name.
    // You can specify a name in DDL, but this does not work in conjunction with
    // temporal tables in certain cases. So we have to delete the constraint with
    // a rather complex statement.
    StringBuilder sb = new StringBuilder();
    if (DdlHelp.isDropDefault(defaultValue)) {
      sb.append("EXEC usp_ebean_drop_default_constraint ").append(tableName).append(", ").append(columnName);
    } else {
      sb.append("alter table ").append(tableName);
      sb.append(" add default ").append(convertDefaultValue(defaultValue)).append(" for ").append(columnName);
    }
    return sb.toString();
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

    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + type + notnullClause;
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

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) throws IOException {

    // do nothing for MS SQL Server (cause it requires stored procedures etc)
  }

  /**
   * Add column comment as a separate statement.
   */
  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) throws IOException {

    // do nothing for MS SQL Server (cause it requires stored procedures etc)
  }

  /**
   * It is rather complex to delete a column on SqlServer as there must not exist any references
   * (constraints, default values, indices and foreign keys). That's why we call a user stored procedure here
   */
  @Override
  public void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) throws IOException {

    buffer.append("EXEC usp_ebean_drop_column ").append(tableName).append(", ").append(columnName).endOfStatement();
  }

  /**
   * This writes the multi value datatypes needed for {@link MultiValueBind}
   */
  @Override
  public void generateProlog(DdlWrite write) throws IOException {
    super.generateProlog(write);

    generateTVPDefinitions(write, "bigint");
    generateTVPDefinitions(write, "float");
    generateTVPDefinitions(write, "bit");
    generateTVPDefinitions(write, "date");
    generateTVPDefinitions(write, "time");
    //generateTVPDefinitions(write, "datetime2");
    generateTVPDefinitions(write, "uniqueidentifier");
    generateTVPDefinitions(write, "nvarchar(max)");

  }

  private void generateTVPDefinitions(DdlWrite write, String definition) throws IOException {
    int pos = definition.indexOf('(');
    String name = pos == -1 ? definition : definition.substring(0, pos);

    dropTVP(write.dropAll(), name);
    //TVPs are included in "I__create_procs.sql"
    //createTVP(write.apply(), name, definition);
  }

  private void dropTVP(DdlBuffer ddl, String name) throws IOException {
    ddl.append("if exists (select name  from sys.types where name = 'ebean_").append(name)
        .append("_tvp') drop type ebean_").append(name).append("_tvp").endOfStatement();
  }

  private void createTVP(DdlBuffer ddl, String name, String definition) throws IOException {
    ddl.append("if not exists (select name  from sys.types where name = 'ebean_").append(name)
    .append("_tvp') create type ebean_").append(name).append("_tvp as table (c1 ").append(definition).append(")")
        .endOfStatement();
  }

}
