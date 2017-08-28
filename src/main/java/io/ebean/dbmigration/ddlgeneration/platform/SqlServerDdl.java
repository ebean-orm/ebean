package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.dbmigration.ddlgeneration.DdlBuffer;
import io.ebean.dbmigration.migration.AlterColumn;
import io.ebean.dbmigration.migration.Column;
import io.ebean.util.StringHelper;

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
    this.inlineUniqueOneToOne = false;
    this.columnSetDefault = "add default";
    this.dropConstraintIfExists = "drop constraint";
    this.historyDdl = new SqlServerHistoryDdl();
  }

  @Override
  public String dropTable(String tableName) {
    return "IF OBJECT_ID('" + tableName + "', 'U') IS NOT NULL drop table " + tableName;
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
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns) {

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

  /**
   * Drop a unique constraint from the table (Sometimes this is an index).
   */
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    return dropIndex(uniqueConstraintName, tableName);
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

    if (isDropDefault(defaultValue)) {
      return "alter table " + tableName + " drop constraint df_" + tableName + "_" + columnName;
    } else {
      return "alter table " + tableName + " add constraint df_" + tableName + "_" + columnName 
          + " default " + defaultValue + " for " + columnName;
    }
  }

  @Override
  public String alterColumnBaseAttributes(AlterColumn alter) {

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
   * Write the column definition to the create table statement.
   */
  @Override
  protected void writeColumnDefinition(DdlBuffer buffer, String tableName, Column column, boolean useIdentity) throws IOException {

    boolean identityColumn = useIdentity && isTrue(column.isPrimaryKey());
    String platformType = convert(column.getType(), identityColumn);

    buffer.append("  ");
    buffer.append(lowerColumnName(column.getName()), 29);
    buffer.append(platformType);
    if (!Boolean.TRUE.equals(column.isPrimaryKey()) && !typeContainsDefault(platformType)) {
      String defaultValue = convertDefaultValue(column.getDefaultValue(), column.getType());
      if (defaultValue != null) {
        buffer.append(" constraint ").append(naming.defaultConstraintName(tableName, column.getName()));
        buffer.append(" default ").append(defaultValue);
      }
    }
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      buffer.append(" not null");
    }

    // add check constraints later as we really want to give them a nice name
    // so that the database can potentially provide a nice SQL error
  }

  public void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column, boolean onHistoryTable, String defaultValue) throws IOException {
    
    String convertedType = convert(column.getType(), false);
    
    buffer.append("alter table ").append(tableName)
      .append(" ").append(addColumn).append(" ").append(column.getName())
      .append(" ").append(convertedType);

    if (!onHistoryTable) {
      if (isTrue(column.isNotnull())) {
        buffer.append(" not null");
      }
      if (!StringHelper.isNull(column.getCheckConstraint())) {
        buffer.append(", constraint ").append(column.getCheckConstraintName());
        buffer.append(" ").append(column.getCheckConstraint());
      }
      
      if (defaultValue != null) {
        if (typeContainsDefault(convertedType)) {
          System.err.println(platform.getName()+ ": Cannot apply default " + defaultValue 
              + " for " + tableName + "." + column.getName() + " " + convertedType);
        } else {
          buffer.append(", constraint df_").append(tableName).append("_").append(column.getName());
          buffer.append(" default ").append(defaultValue).append(" for ").append(column.getName());
        }
      }
    }
    buffer.endOfStatement();
  }
}
