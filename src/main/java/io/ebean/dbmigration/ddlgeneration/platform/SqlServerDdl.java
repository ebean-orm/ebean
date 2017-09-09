package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.dbmigration.ddlgeneration.DdlBuffer;
import io.ebean.dbmigration.migration.AlterColumn;

import java.io.IOException;

/**
 * MS SQL Server platform specific DDL.
 */
public class SqlServerDdl extends PlatformDdl {

  public SqlServerDdl(DatabasePlatform platform) {
    super(platform);
    this.identitySuffix = " identity(1,1)";
    this.foreignKeyRestrict = "";
    this.alterTableIfExists = "";
    this.addColumn = "add";
    this.inlineUniqueWhenNullable = false;
    this.columnSetDefault = "add default";
    this.dropConstraintIfExists = "drop constraint";
    this.historyDdl = new SqlServerHistoryDdl();
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
    String objectId = fkName;
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
    return "IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('" + tableName +"','U') AND name = '" + indexName + "') drop index " + indexName + " ON " + tableName;
  }
  /**
   * MsSqlServer specific null handling on unique constraints.
   */
  @Override
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, boolean notNull) {
    if (notNull) {
      return super.alterTableAddUniqueConstraint(tableName, uqName, columns, notNull);
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
    for (String column : columns) {
      sb.append(sep).append(column).append(" is not null");
      sep = " and ";
    }
    return sb.toString();
  }
  
  public String alterTableDropConstraint(String tableName, String constraintName) {
    StringBuilder sb = new StringBuilder();
    sb.append("IF (OBJECT_ID('").append(constraintName).append("', 'C') IS NOT NULL) ");
    sb.append(super.alterTableDropConstraint(tableName, constraintName));
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
    if (allocationSize > 0 && allocationSize != 50) {
      // at this stage ignoring allocationSize 50 as this is the 'default' and
      // not consistent with the way Ebean batch fetches sequence values
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
      sb.append("delimiter $$\n");
      sb.append("DECLARE @Tmp nvarchar(200);");
      sb.append("select @Tmp = t1.name  from sys.default_constraints t1\n");
      sb.append("  join sys.columns t2 on t1.object_id = t2.default_object_id\n");
      sb.append("  where t1.parent_object_id = OBJECT_ID('").append(tableName)
        .append("') and t2.name = '").append(columnName).append("';\n");
      sb.append("if @Tmp is not null EXEC('alter table ").append(tableName).append(" drop constraint ' + @Tmp)$$");
    } else {
      sb.append("alter table ").append(tableName);
      sb.append(" add default ").append(defaultValue).append(" for ").append(columnName);
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
}
