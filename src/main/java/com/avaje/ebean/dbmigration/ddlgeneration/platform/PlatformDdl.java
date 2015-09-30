package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.dbmigration.ddlgeneration.BaseDdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.IdentityType;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.List;

/**
 * Controls the DDL generation for a specific database platform.
 */
public class PlatformDdl {

  protected PlatformHistoryDdl historyDdl = new NoHistorySupportDdl();

  /**
   * Converter for logical/standard types to platform specific types. (eg. clob -> text)
   */
  private final PlatformTypeConverter typeConverter;

  /**
   * For handling support of sequences and autoincrement.
   */
  private final DbIdentity dbIdentity;

  /**
   * Default assumes if exists is supported.
   */
  protected String dropTableIfExists = "drop table if exists ";

  protected String dropTableCascade = "";

  /**
   * Default assumes if exists is supported.
   */
  protected String dropSequenceIfExists = "drop sequence if exists ";

  protected String foreignKeyRestrict = "on delete restrict on update restrict";

  protected String identitySuffix = " auto_increment";

  protected String dropConstraintIfExists = "drop constraint if exists";

  protected String dropIndexIfExists = "drop index if exists ";

  protected String alterColumn = "alter column";

  protected String dropUniqueConstraint = "drop constraint";

  protected String columnSetType = "";

  protected String columnSetDefault = "set default";

  protected String columnDropDefault = "drop default";

  protected String columnSetNotnull = "set not null";

  protected String columnSetNull = "set null";

  /**
   * Set false for MsSqlServer to allow multiple nulls for OneToOne mapping.
   */
  protected boolean inlineUniqueOneToOne = true;

  protected DbConstraintNaming naming;

  public PlatformDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    this.dbIdentity = dbIdentity;
    this.typeConverter = new PlatformTypeConverter(platformTypes);
  }

  /**
   * Set configuration options.
   */
  public void configure(ServerConfig serverConfig) {
    historyDdl.configure(serverConfig, this);
    naming = serverConfig.getConstraintNaming();
  }

  /**
   * Create a DdlHandler for the specific database platform.
   */
  public DdlHandler createDdlHandler(ServerConfig serverConfig) {
    return new BaseDdlHandler(serverConfig, this);
  }

  /**
   * Return the identity type to use given the support in the underlying database
   * platform for sequences and identity/autoincrement.
   */
  public IdType useIdentityType(IdentityType modelIdentityType) {

    return dbIdentity.useIdentityType(modelIdentityType);
  }

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  public String asIdentityColumn(String columnDefn) {
    return columnDefn + identitySuffix;
  }

  /**
   * Write all the table columns converting to platform types as necessary.
   */
  public void writeTableColumns(DdlBuffer apply, List<Column> columns, boolean useIdentity) throws IOException {
    for (int i = 0; i < columns.size(); i++) {
      apply.newLine();
      writeColumnDefinition(apply, columns.get(i), useIdentity);
      if (i < columns.size() - 1) {
        apply.append(",");
      }
    }
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, Column column, boolean useIdentity) throws IOException {

    boolean identityColumn = useIdentity && isTrue(column.isPrimaryKey());
    String platformType = convert(column.getType(), identityColumn);

    buffer.append("  ");
    buffer.append(lowerColumnName(column.getName()), 29);
    buffer.append(platformType);
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      buffer.append(" not null");
    }

    // add check constraints later as we really want to give them a nice name
    // so that the database can potentially provide a nice SQL error
  }

  /**
   * Return the drop foreign key clause.
   */
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + tableName + " " + dropConstraintIfExists + " " + fkName;
  }

  /**
   * Convert the standard type to the platform specific type.
   */
  public String convert(String type, boolean identity) {
    String platformType = typeConverter.convert(type);
    return identity ? asIdentityColumn(platformType) : platformType;
  }

  /**
   * Add history support to this table using the platform specific mechanism.
   */
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    historyDdl.createWithHistory(writer, table);
  }

  /**
   * Drop history support for a given table.
   */
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {
    historyDdl.dropHistoryTable(writer, dropHistoryTable);
  }

  /**
   * Add history support to an existing table.
   */
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {
    historyDdl.addHistoryTable(writer, addHistoryTable);
  }

  /**
   * Regenerate the history triggers (or function) due to a column being added/dropped/excluded or included.
   */
  public void regenerateHistoryTriggers(DdlWrite write, HistoryTableUpdate update) throws IOException {
    historyDdl.regenerateHistoryTriggers(write, update);
  }

  /**
   * Generate and return the create sequence DDL.
   */
  public String createSequence(String sequenceName, int initialValue, int allocationSize) {

    StringBuilder sb = new StringBuilder("create sequence ");
    sb.append(sequenceName);
    if (initialValue > 1) {
      sb.append(" start with ").append(initialValue);
    }
    if (allocationSize > 0 && allocationSize != 50) {
      // at this stage ignoring allocationSize 50 as this is the 'default' and
      // not consistent with the way Ebean batch fetches sequence values
      sb.append(" increment by ").append(allocationSize);
    }
    sb.append(";");
    return sb.toString();
  }

  /**
   * Return the drop sequence statement (potentially with if exists clause).
   */
  public String dropSequence(String sequenceName) {
    return dropSequenceIfExists + sequenceName;
  }

  /**
   * Return the drop table statement (potentially with if exists clause).
   */
  public String dropTable(String tableName) {
    return dropTableIfExists + tableName + dropTableCascade;
  }

  /**
   * Return the drop index statement.
   */
  public String dropIndex(String indexName, String tableName) {
    return dropIndexIfExists + indexName;
  }

  /**
   * Return the create index statement.
   */
  public String createIndex(String indexName, String tableName, String[] columns) {

    StringBuilder buffer = new StringBuilder();
    buffer.append("create index ").append(indexName).append(" on ").append(tableName);
    appendColumns(columns, buffer);

    return buffer.toString();
  }

  /**
   * Add foreign key.
   */
  public String alterTableAddForeignKey(String tableName, String fkName, String[] columns, String refTable, String[] refColumns) {

    StringBuilder buffer = new StringBuilder(90);
    buffer
        .append("alter table ").append(tableName)
        .append(" add constraint ").append(fkName)
        .append(" foreign key");
    appendColumns(columns, buffer);
    buffer
        .append(" references ")
        .append(lowerTableName(refTable));
    appendColumns(refColumns, buffer);
    appendWithSpace(foreignKeyRestrict, buffer);

    return buffer.toString();
  }

  /**
   * Drop a unique constraint from the table.
   */
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    return "alter table " + tableName + " " + dropUniqueConstraint + " " + uniqueConstraintName;
  }

  /**
   * Add a unique constraint to the table.
   * <p>
   * Overridden by MsSqlServer for specific null handling on unique constraints.
   */
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns) {

    StringBuilder buffer = new StringBuilder(90);
    buffer.append("alter table ").append(tableName).append(" add constraint ").append(uqName).append(" unique ");
    appendColumns(columns, buffer);
    return buffer.toString();
  }

  /**
   * Return true if unique constraints for OneToOne can be inlined as normal.
   * Returns false for MsSqlServer due to it's null handling for unique constraints.
   */
  public boolean isInlineUniqueOneToOne() {
    return inlineUniqueOneToOne;
  }

  /**
   * Alter a column type.
   * <p>
   * Note that that MySql and SQL Server instead use alterColumnBaseAttributes()
   * </p>
   */
  public String alterColumnType(String tableName, String columnName, String type) {

    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + columnSetType + type;
  }

  /**
   * Alter a column adding or removing the not null constraint.
   * <p>
   * Note that that MySql and SQL Server instead use alterColumnBaseAttributes()
   * </p>
   */
  public String alterColumnNotnull(String tableName, String columnName, boolean notnull) {

    String suffix = notnull ? columnSetNotnull : columnSetNull;
    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + suffix;
  }

  /**
   * Return true if the default value is the special DROP DEFAULT value.
   */
  public boolean isDropDefault(String defaultValue) {
    return "DROP DEFAULT".equals(defaultValue);
  }

  /**
   * Alter column setting the default value.
   */
  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {

    String suffix = isDropDefault(defaultValue) ? columnDropDefault : columnSetDefault + " " + defaultValue;
    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + suffix;
  }

  /**
   * Alter column setting both the type and not null constraint.
   * <p>
   * Used by MySql and SQL Server as these require both column attributes to be set together.
   * </p>
   */
  public String alterColumnBaseAttributes(AlterColumn alter) {
    // by default do nothing, only used by mysql and sql server as they can only
    // modify the column with the full column definition
    return null;
  }

  protected void appendColumns(String[] columns, StringBuilder buffer) {
    buffer.append(" (");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(lowerColumnName(columns[i].trim()));
    }
    buffer.append(")");
  }

  protected void appendWithSpace(String content, StringBuilder buffer) {
    if (content != null && !content.isEmpty()) {
      buffer.append(" ").append(content);
    }
  }

  /**
   * Convert the table to lower case.
   * <p>
   * Override as desired. Generally lower case with underscore is a good cross database
   * choice for column/table names.
   */
  protected String lowerTableName(String name) {
    return naming.lowerTableName(name);
  }

  /**
   * Convert the column name to lower case.
   * <p>
   * Override as desired. Generally lower case with underscore is a good cross database
   * choice for column/table names.
   */
  protected String lowerColumnName(String name) {
    return naming.lowerColumnName(name);
  }


  /**
   * Null safe Boolean true test.
   */
  protected boolean isTrue(Boolean value) {
    return Boolean.TRUE.equals(value);
  }
}
