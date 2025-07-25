package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AlterColumn;

/**
 * MS SQL Server platform specific DDL.
 */
public class SqlServerDdl extends PlatformDdl {

  private static final String CONSTRAINT = "C";
  private static final String UNIQUE_CONSTRAINT = "UQ";
  private static final String USER_TABLE = "U";
  private static final String FOREIGN_KEY = "F";
  private static final String SEQUENCE_OBJECT = "SO";

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
    return ifObjectExists(tableName, USER_TABLE) + "drop table " + tableName;
  }

  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    int pos = tableName.lastIndexOf('.');
    String objectId = maxConstraintName(fkName);
    if (pos != -1) {
      objectId = tableName.substring(0, pos + 1) + fkName;
    }
    return ifObjectExists(objectId, FOREIGN_KEY) + super.alterTableDropForeignKey(tableName, fkName);
  }

  @Override
  public String dropSequence(String sequenceName) {
    return ifObjectExists(sequenceName, SEQUENCE_OBJECT) + "drop sequence " + sequenceName;
  }

  @Override
  public String dropIndex(String indexName, String tableName, boolean concurrent) {
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
    StringBuilder sb = new StringBuilder(256);
    sb.append("create unique nonclustered index ").append(uqName).append(" on ").append(tableName).append('(');
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append(',');
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
    return ifObjectExists(maxConstraintName(constraintName), CONSTRAINT) + super.alterTableDropConstraint(tableName, constraintName);
  }

  /**
   * Drop a unique constraint from the table (Sometimes this is an index).
   */
  @Override
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    StringBuilder sb = new StringBuilder();
    sb.append(ifObjectExists(maxConstraintName(uniqueConstraintName), UNIQUE_CONSTRAINT))
      .append(super.alterTableDropUniqueConstraint(tableName, uniqueConstraintName)).append(";\n");

    sb.append(dropIndex(uniqueConstraintName, tableName));
    return sb.toString();
  }

  /**
   * Generate and return the create sequence DDL.
   */
  @Override
  public String createSequence(String sequenceName, DdlIdentity identity) {
    StringBuilder sb = new StringBuilder(80);
    sb.append("create sequence ").append(sequenceName).append(" as bigint");
    final int start = identity.getStart();
    if (start > 1) {
      sb.append(" start with ").append(start);
    } else {
      sb.append(" start with 1");
    }
    final int increment = identity.getIncrement();
    if (increment > 1) {
      sb.append(" increment by ").append(increment);
    }
    final int cache = identity.getCache();
    if (cache > 1) {
      sb.append(" cache ").append(increment);
    }
    sb.append(';');
    return sb.toString();
  }

  @Override
  protected void alterColumnDefault(DdlWrite writer, AlterColumn alter) {
    // Unfortunately, the SqlServer creates default values with a random name.
    // You can specify a name in DDL, but this does not work in conjunction with
    // temporal tables in certain cases. So we have to delete the constraint with
    // a rather complex statement.
    String tableName = alter.getTableName();
    String columnName = alter.getColumnName();
    String defaultValue = alter.getDefaultValue();
    if (DdlHelp.isDropDefault(defaultValue)) {
      execUspDropDefaultConstraint(writer, tableName, columnName);
    } else {
      execUspDropDefaultConstraint(writer, tableName, columnName);
      setDefaultValue(writer, tableName, columnName, defaultValue);
    }
  }

  @Override
  public void alterColumn(DdlWrite writer, AlterColumn alter) {
    String tableName = alter.getTableName();
    String columnName = alter.getColumnName();
    if (alter.getType() == null && alter.isNotnull() == null) {
      // No type change or notNull change
      if (hasValue(alter.getDefaultValue())) {
        alterColumnDefault(writer, alter);
      }
    } else {
      // we must regenerate whole statement -> read altered and current value
      String type = alter.getType() != null ? alter.getType() : alter.getCurrentType();
      type = convert(type);
      boolean notnull = (alter.isNotnull() != null) ? alter.isNotnull() : Boolean.TRUE.equals(alter.isCurrentNotnull());
      String defaultValue = alter.getDefaultValue() != null ? alter.getDefaultValue() : alter.getCurrentDefaultValue();
      if (hasValue(defaultValue)) {
        // default value present -> drop default constraint before altering
        execUspDropDefaultConstraint(writer, tableName, columnName);
      }

      DdlBuffer buffer = alterTable(writer, tableName).append(alterColumn, columnName);
      buffer.append(type);
      if (notnull) {
        buffer.append(" not null");
      }

      // re add - default constraint
      if (hasValue(defaultValue) && !DdlHelp.isDropDefault(defaultValue)) {
        setDefaultValue(writer, tableName, columnName, defaultValue);
      }
    }
  }

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {

    // do nothing for MS SQL Server (cause it requires stored procedures etc)
  }

  /**
   * Add column comment as a separate statement.
   */
  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {

    // do nothing for MS SQL Server (cause it requires stored procedures etc)
  }

  /**
   * It is rather complex to delete a column on SqlServer as there must not exist any references
   * (constraints, default values, indices and foreign keys). That's why we call a user stored procedure here
   */
  @Override
  public void alterTableDropColumn(DdlWrite writer, String tableName, String columnName) {
    alterTable(writer, tableName).raw("EXEC usp_ebean_drop_column ").append(tableName).append(", ").append(columnName);
  }

  /**
   * This writes the multi value datatypes needed for MultiValueBind.
   */
  @Override
  public void generateProlog(DdlWrite writer) {
    super.generateProlog(writer);

    generateTVPDefinitions(writer, "bigint");
    generateTVPDefinitions(writer, "float");
    generateTVPDefinitions(writer, "bit");
    generateTVPDefinitions(writer, "date");
    generateTVPDefinitions(writer, "time");
    //generateTVPDefinitions(write, "datetime2");
    generateTVPDefinitions(writer, "uniqueidentifier");
    generateTVPDefinitions(writer, "nvarchar(max)");

  }

  private void generateTVPDefinitions(DdlWrite writer, String definition) {
    int pos = definition.indexOf('(');
    String name = pos == -1 ? definition : definition.substring(0, pos);

    dropTVP(writer.dropAll(), name);
    //TVPs are included in "I__create_procs.sql"
    //createTVP(write.apply(), name, definition);
  }

  private void dropTVP(DdlBuffer ddl, String name) {
    ddl.append("if exists (select name  from sys.types where name = 'ebean_").append(name)
        .append("_tvp') drop type ebean_").append(name).append("_tvp").endOfStatement();
  }

  @SuppressWarnings("unused")
  private void createTVP(DdlBuffer ddl, String name, String definition) {
    ddl.append("if not exists (select name  from sys.types where name = 'ebean_").append(name)
    .append("_tvp') create type ebean_").append(name).append("_tvp as table (c1 ").append(definition).append(")")
        .endOfStatement();
  }

  public static String ifObjectExists(String object, String objectType) {
    return "IF OBJECT_ID('" + object + "', '" + objectType + "') IS NOT NULL ";
  }

  private void execUspDropDefaultConstraint(DdlWrite writer, String tableName, String columnName) {
    alterTable(writer, tableName).raw("EXEC usp_ebean_drop_default_constraint " + tableName + ", " + columnName);
  }

  private void setDefaultValue(DdlWrite writer, String tableName, String columnName, String defaultValue) {
    alterTable(writer, tableName).append("add default " + convertDefaultValue(defaultValue) + " for", columnName);
  }
}
