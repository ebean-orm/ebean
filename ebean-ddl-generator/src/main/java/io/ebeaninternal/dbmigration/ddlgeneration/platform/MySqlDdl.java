package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;

/**
 * MySql specific DDL.
 */
public class MySqlDdl extends PlatformDdl {

  // check constraint support is disabled by default. See https://groups.google.com/forum/#!topic/ebean/luFN-2xBkUw
  // this flag is for compatibility. Use it with care.
  private static final boolean USE_CHECK_CONSTRAINT = Boolean.getBoolean("ebean.mysql.useCheckConstraint");

  private final boolean useMigrationStoredProcedures;

  public MySqlDdl(DatabasePlatform platform) {
    super(platform);
    this.alterColumn = "alter";
    this.dropUniqueConstraint = "drop index";
    this.historyDdl = new MySqlHistoryDdl();
    this.inlineComments = true;
    this.useMigrationStoredProcedures = platform.useMigrationStoredProcedures();
  }

  /**
   * Return the drop index statement.
   */
  @Override
  public String dropIndex(String indexName, String tableName, boolean concurrent) {
    return "drop index " + maxConstraintName(indexName) + " on " + quote(tableName);
  }

  @Override
  public void alterTableDropColumn(DdlWrite writer, String tableName, String columnName) {
    if (this.useMigrationStoredProcedures) {
      alterTable(writer, tableName).raw("CALL usp_ebean_drop_column('").append(naming.normaliseTable(tableName))
        .append("', '").append(naming.normaliseColumn(columnName)).append("')");
    } else {
      super.alterTableDropColumn(writer, tableName, columnName);
    }
  }

  /**
   * Return the drop foreign key clause.
   */
  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + quote(tableName) + " drop foreign key " + maxConstraintName(fkName);
  }

  @Override
  public String createCheckConstraint(String ckName, String checkConstraint) {
    if (USE_CHECK_CONSTRAINT) {
      return super.createCheckConstraint(ckName, checkConstraint);
    } else {
      return null;
    }
  }

  @Override
  public String alterTableAddCheckConstraint(String tableName, String checkConstraintName, String checkConstraint) {
    if (USE_CHECK_CONSTRAINT) {
      return super.alterTableAddCheckConstraint(tableName, checkConstraintName, checkConstraint);
    } else {
      return null;
    }
  }

  @Override
  public String alterTableDropConstraint(String tableName, String constraintName) {
    // drop constraint not supported in MySQL 5.7 and 8.0 but starting with MariaDB
    // 10.2.1 CHECK is evaluated
    if (USE_CHECK_CONSTRAINT) {
      StringBuilder sb = new StringBuilder();
      // statement for MySQL >= 8.0.16
      sb.append("/*!80016 alter table ").append(tableName);
      sb.append(" drop check ").append(constraintName).append(" */;\n");
      // statement for MariaDB >= 10.2.1
      sb.append("/*M!100201 ");
      sb.append(super.alterTableDropConstraint(tableName, constraintName));
      sb.append(" */");
      return sb.toString();
    } else {
      return null;
    }
  }

  @Override
  public void alterColumn(DdlWrite writer, AlterColumn alter) {
    String tableName = alter.getTableName();
    String columnName = alter.getColumnName();
    boolean commentChange = hasValue(alter.getComment());

    if (alter.getType() == null && alter.isNotnull() == null && !commentChange) {
      // No type change, notNull change or comment change -> handle default value change
      if (hasValue(alter.getDefaultValue())) {
        alterColumnDefault(writer, alter);
      }
    } else {
      // we must regenerate whole statement -> read altered and current value
      String type = alter.getType() != null ? alter.getType() : alter.getCurrentType();
      type = convert(type);
      boolean notnull = (alter.isNotnull() != null) ? alter.isNotnull() : Boolean.TRUE.equals(alter.isCurrentNotnull());
      String defaultValue = alter.getDefaultValue() != null ? alter.getDefaultValue() : alter.getCurrentDefaultValue();

      DdlBuffer buffer = alterTable(writer, tableName).append("modify", columnName);
      buffer.append(type);
      if (notnull) {
        buffer.append(" not null");
      }
      if (hasValue(defaultValue) && !DdlHelp.isDropDefault(defaultValue)) {
        buffer.append(" default ").append(convertDefaultValue(defaultValue));
      }
      // restate the comment (new, existing, or none) as mysql requires the whole column
      // definition to be repeated - otherwise a comment could be silently dropped
      String comment = alter.getComment() != null ? alter.getComment() : alter.getCurrentComment();
      if (DdlHelp.isDropComment(comment)) {
        comment = null;
      }
      appendColumnComment(buffer, comment);
    }
  }

  @Override
  public void alterTableAddColumn(DdlWrite writer, String tableName, Column column, boolean onHistoryTable, String defaultValue) {
    String convertedType = convert(column.getType());
    DdlBuffer buffer = alterTable(writer, tableName).append(addColumn, column.getName());
    buffer.append(convertedType);

    // Add default value also to history table if it is not excluded
    if (defaultValue != null) {
      if (!onHistoryTable || !isTrue(column.isHistoryExclude())) {
        buffer.append(" default ");
        buffer.append(defaultValue);
      }
    }
    if (!onHistoryTable) {
      if (isTrue(column.isNotnull())) {
        buffer.appendWithSpace(columnNotNull);
      }
      // check constraints cannot be added in one statement for h2
      if (!StringHelper.isNull(column.getCheckConstraint())) {
        String ddl = alterTableAddCheckConstraint(tableName, column.getCheckConstraintName(), column.getCheckConstraint());
        writer.applyPostAlter().appendStatement(ddl);
      }
      // comment must be inline as part of the column definition for mysql
      appendColumnComment(buffer, column.getComment());
    }
  }

  @Override
  protected void writeColumnDefinition(DdlBuffer buffer, Column column, DdlIdentity identity) {
    super.writeColumnDefinition(buffer, column, identity);
    appendColumnComment(buffer, column.getComment());
  }

  private void appendColumnComment(DdlBuffer buffer, String comment) {
    if (!StringHelper.isNull(comment)) {
      // in mysql 5.5 column comment save in information_schema.COLUMNS.COLUMN_COMMENT(VARCHAR 1024)
      if (comment.length() > 500) {
        comment = comment.substring(0, 500);
      }
      buffer.append(String.format(" comment '%s'", comment));
    }
  }

  @Override
  public void inlineTableComment(DdlBuffer apply, String tableComment) {
    if (tableComment.length() > 1000) {
      tableComment = tableComment.substring(0, 1000);
    }
    apply.append(" comment='").append(tableComment).append("'");
  }

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    if (DdlHelp.isDropComment(tableComment)) {
      tableComment = "";
    }
    apply.append(String.format("alter table %s comment = '%s'", quote(tableName), tableComment)).endOfStatement();
  }

  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    // alter comment currently not supported as it requires to repeat whole column definition
  }

}
