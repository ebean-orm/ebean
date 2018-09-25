package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbDefaultValue;
import io.ebean.config.dbplatform.DbIdentity;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.VowelRemover;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import io.ebeaninternal.dbmigration.model.MTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Controls the DDL generation for a specific database platform.
 */
public class PlatformDdl {

  private static final Logger logger = LoggerFactory.getLogger(PlatformDdl.class);

  protected final DatabasePlatform platform;

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
   * Set to true if table and column comments are included inline with the create statements.
   */
  protected boolean inlineComments;

  /**
   * Default assumes if exists is supported.
   */
  protected String dropTableIfExists = "drop table if exists ";

  protected String dropTableCascade = "";

  /**
   * Default assumes if exists is supported.
   */
  protected String dropSequenceIfExists = "drop sequence if exists ";

  protected String foreignKeyOnDelete = "on delete";
  protected String foreignKeyOnUpdate = "on update";

  protected String identitySuffix = " auto_increment";

  protected String alterTableIfExists = "";

  protected String dropConstraintIfExists = "drop constraint if exists";

  protected String dropIndexIfExists = "drop index if exists ";

  protected String alterColumn = "alter column";

  protected String dropUniqueConstraint = "drop constraint";

  protected String addConstraint = "add constraint";

  protected String addColumn = "add column";

  protected String columnSetType = "";

  protected String columnSetDefault = "set default";

  protected String columnDropDefault = "drop default";

  protected String columnSetNotnull = "set not null";

  protected String columnSetNull = "set null";

  protected String updateNullWithDefault = "update ${table} set ${column} = ${default} where ${column} is null";

  /**
   * Set false for MsSqlServer to allow multiple nulls for OneToOne mapping.
   */
  protected boolean inlineUniqueWhenNullable = true;

  protected DbConstraintNaming naming;

  /**
   * Generally not desired as then they are not named (used with SQLite).
   */
  protected boolean inlineForeignKeys;

  protected final DbDefaultValue dbDefaultValue;

  protected String fallbackArrayType = "varchar(1000)";

  public PlatformDdl(DatabasePlatform platform) {
    this.platform = platform;
    this.dbIdentity = platform.getDbIdentity();
    this.dbDefaultValue = platform.getDbDefaultValue();
    this.typeConverter = new PlatformTypeConverter(platform.getDbTypeMap());
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
  public IdType useIdentityType(IdentityType modelIdentity) {

    if (modelIdentity == null) {
      // use the default
      return dbIdentity.getIdType();
    }
    return identityType(modelIdentity, dbIdentity.getIdType(), dbIdentity.isSupportsSequence(), dbIdentity.isSupportsIdentity());
  }

  /**
   * Determine the id type to use based on requested identityType and
   * the support for that in the database platform.
   */
  private IdType identityType(IdentityType modelIdentity, IdType platformIdType, boolean supportsSequence, boolean supportsIdentity) {

    switch (modelIdentity) {
      case GENERATOR:
        return IdType.GENERATOR;
      case EXTERNAL:
        return IdType.EXTERNAL;
      case SEQUENCE:
        return supportsSequence ? IdType.SEQUENCE : platformIdType;
      case IDENTITY:
        return supportsIdentity ? IdType.IDENTITY : platformIdType;
      default:
        return platformIdType;
    }
  }

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  public String asIdentityColumn(String columnDefn) {
    return columnDefn + identitySuffix;
  }

  /**
   * Return true if the table and column comments are included inline.
   */
  public boolean isInlineComments() {
    return inlineComments;
  }

  /**
   * Return true if foreign key reference constraints need to inlined with create table.
   * Ideally we don't do this as then the constraints are not named. Do this for SQLite.
   */
  public boolean isInlineForeignKeys() {
    return inlineForeignKeys;
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
    if (!Boolean.TRUE.equals(column.isPrimaryKey())) {
      String defaultValue = convertDefaultValue(column.getDefaultValue());
      if (defaultValue != null) {
        buffer.append(" default ").append(defaultValue);
      }
    }
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      buffer.append(" not null");
    }

    // add check constraints later as we really want to give them a nice name
    // so that the database can potentially provide a nice SQL error
  }

  /**
   * Convert the DB column default literal to platform specific.
   */
  public String convertDefaultValue(String dbDefault) {
    return dbDefaultValue.convert(dbDefault);
  }

  /**
   * Return the drop foreign key clause.
   */
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + alterTableIfExists + tableName + " " + dropConstraintIfExists + " " + maxConstraintName(fkName);
  }

  /**
   * Convert the standard type to the platform specific type.
   */
  public String convert(String type, boolean identity) {
    if (type == null) {
      return null;
    }
    if (type.contains("[]")) {
      return convertArrayType(type);
    }
    String platformType = typeConverter.convert(type);
    return identity ? asIdentityColumn(platformType) : platformType;
  }

  /**
   * Convert the logical array type to a db platform specific type to support the array data.
   */
  protected String convertArrayType(String logicalArrayType) {
    if (logicalArrayType.endsWith("]")) {
      return fallbackArrayType;
    }
    int colonPos = logicalArrayType.lastIndexOf(']');
    return "varchar" + logicalArrayType.substring(colonPos + 1);
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
    historyDdl.updateTriggers(write, update);
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
    if (allocationSize > 1) {
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
    return dropIndexIfExists + maxConstraintName(indexName);
  }

  /**
   * Return the create index statement.
   */
  public String createIndex(String indexName, String tableName, String[] columns) {

    StringBuilder buffer = new StringBuilder();
    buffer.append("create index ").append(maxConstraintName(indexName)).append(" on ").append(tableName);
    appendColumns(columns, buffer);

    return buffer.toString();
  }

  /**
   * Return the foreign key constraint when used inline with create table.
   */
  public String tableInlineForeignKey(WriteForeignKey request) {

    StringBuilder buffer = new StringBuilder(90);
    buffer.append("foreign key");
    appendColumns(request.cols(), buffer);
    buffer.append(" references ").append(lowerTableName(request.refTable()));
    appendColumns(request.refCols(), buffer);
    appendForeignKeySuffix(request, buffer);
    return buffer.toString();
  }

  /**
   * Add foreign key.
   */
  public String alterTableAddForeignKey(WriteForeignKey request) {

    StringBuilder buffer = new StringBuilder(90);
    buffer
      .append("alter table ").append(lowerTableName(request.table()))
      .append(" add constraint ").append(maxConstraintName(request.fkName()))
      .append(" foreign key");
    appendColumns(request.cols(), buffer);
    buffer
      .append(" references ")
      .append(lowerTableName(request.refTable()));
    appendColumns(request.refCols(), buffer);
    appendForeignKeySuffix(request, buffer);
    return buffer.toString();
  }

  protected void appendForeignKeySuffix(WriteForeignKey request, StringBuilder buffer) {
    appendForeignKeyOnDelete(buffer, withDefault(request.onDelete()));
    appendForeignKeyOnUpdate(buffer, withDefault(request.onDelete()));
  }

  protected ConstraintMode withDefault(ConstraintMode mode) {
    return (mode == null) ? ConstraintMode.RESTRICT : mode;
  }

  protected void appendForeignKeyOnDelete(StringBuilder buffer, ConstraintMode mode) {
    appendForeignKeyMode(buffer, foreignKeyOnDelete, mode);
  }

  protected void appendForeignKeyOnUpdate(StringBuilder buffer, ConstraintMode mode) {
    appendForeignKeyMode(buffer, foreignKeyOnUpdate, mode);
  }

  protected void appendForeignKeyMode(StringBuilder buffer, String onMode, ConstraintMode mode) {
    buffer.append(" ").append(onMode).append(" ").append(translate(mode));
  }

  protected String translate(ConstraintMode mode) {
    switch(mode) {
      case SET_NULL: return "set null";
      case SET_DEFAULT: return "set default";
      case RESTRICT: return "restrict";
      case CASCADE: return "cascade";
      default:
        throw new IllegalStateException("Unknown mode "+mode);
    }
  }

  /**
   * Drop a unique constraint from the table (Sometimes this is an index).
   */
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    return "alter table " + tableName + " " + dropUniqueConstraint + " " + maxConstraintName(uniqueConstraintName);
  }

  /**
   * Drop a unique constraint from the table.
   */
  public String alterTableDropConstraint(String tableName, String constraintName) {
    return "alter table " + tableName + " " + dropConstraintIfExists + " " + maxConstraintName(constraintName);
  }

  /**
   * Add a unique constraint to the table.
   * <p>
   * Overridden by MsSqlServer for specific null handling on unique constraints.
   */
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, String[] nullableColumns) {

    StringBuilder buffer = new StringBuilder(90);
    buffer.append("alter table ").append(tableName).append(" add constraint ").append(maxConstraintName(uqName)).append(" unique ");
    appendColumns(columns, buffer);
    return buffer.toString();
  }

  public void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column, boolean onHistoryTable, String defaultValue) throws IOException {

    String convertedType = convert(column.getType(), false);

    buffer.append("alter table ").append(tableName)
      .append(" ").append(addColumn).append(" ").append(column.getName())
      .append(" ").append(convertedType);


    // Add default value also to history table if it is not excluded
    if (defaultValue != null) {
      if (!onHistoryTable || !isTrue(column.isHistoryExclude())) {
        buffer.append(" default ");
        buffer.append(defaultValue);
      }
    }

    if (!onHistoryTable) {
      if (isTrue(column.isNotnull())) {
        buffer.append(" not null");
      }
      buffer.endOfStatement();

      // check constraints cannot be added in one statement for h2
      if (!StringHelper.isNull(column.getCheckConstraint())) {
        String ddl = alterTableAddCheckConstraint(tableName, column.getCheckConstraintName(),
            column.getCheckConstraint());
        buffer.append(ddl).endOfStatement();
      }
    } else {
      buffer.endOfStatement();
    }

  }

  public void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) throws IOException {
    buffer.append("alter table ").append(tableName).append(" drop column ").append(columnName)
    .endOfStatement();
  }

  /**
   * Return true if unique constraints for nullable columns can be inlined as normal.
   * Returns false for MsSqlServer & DB2 due to it's not possible to to put a constraint
   * on a nullable column
   */
  public boolean isInlineUniqueWhenNullable() {
    return inlineUniqueWhenNullable;
  }

  /**
   * Alter a column type.
   * <p>
   * Note that that MySql and SQL Server instead use alterColumnBaseAttributes()
   * </p>
   */
  public String alterColumnType(String tableName, String columnName, String type) {

    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + columnSetType + convert(type, false);
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
   * Alter table adding the check constraint.
   */
  public String alterTableAddCheckConstraint(String tableName, String checkConstraintName, String checkConstraint) {

    return "alter table " + tableName + " " + addConstraint + " " + maxConstraintName(checkConstraintName) + " " + checkConstraint;
  }

  /**
   * Alter column setting the default value.
   */
  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {
    String suffix = DdlHelp.isDropDefault(defaultValue) ? columnDropDefault : columnSetDefault + " " + convertDefaultValue(defaultValue);
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

  public DatabasePlatform getPlatform() {
    return platform;
  }

  public String getUpdateNullWithDefault() {
    return updateNullWithDefault;
  }

  /**
   * Null safe Boolean true test.
   */
  protected boolean isTrue(Boolean value) {
    return Boolean.TRUE.equals(value);
  }

  /**
   * Add an inline table comment to the create table statement.
   */
  public void inlineTableComment(DdlBuffer apply, String tableComment) throws IOException {
    // do nothing by default (MySql only)
  }

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) throws IOException {
    if (DdlHelp.isDropComment(tableComment)) {
      tableComment = "";
    }
    apply.append(String.format("comment on table %s is '%s'", tableName, tableComment)).endOfStatement();
  }

  /**
   * Add column comment as a separate statement.
   */
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) throws IOException {
    if (DdlHelp.isDropComment(comment)) {
      comment = "";
    }
    apply.append(String.format("comment on column %s.%s is '%s'", table, column, comment)).endOfStatement();
  }

  /**
   * Use this to generate a prolog for each script (stored procedures)
   */
  public void generateProlog(DdlWrite write) throws IOException {

  }

  /**
   * Use this to generate an epilog. Will be added at the end of script
   */
  public void generateEpilog(DdlWrite write) throws IOException {

  }

  /**
   * Shortens the given name to the maximum constraint name length of the platform in a deterministic way.
   *
   * First, all vowels are removed, If the string is still to long, 31 bits are taken from the hash code
   * of the string and base36 encoded (10 digits and 26 chars) string.
   *
   * As 36^6 > 31^2, the resulting string is never longer as 6 chars.
   */
  protected String maxConstraintName(String name) {
    if (name.length() > platform.getMaxConstraintNameLength()) {
      int hash = name.hashCode() & 0x7FFFFFFF;
      name = VowelRemover.trim(name, 4);
      if (name.length() > platform.getMaxConstraintNameLength()) {
        return name.substring(0, platform.getMaxConstraintNameLength()-7) + "_" + Integer.toString(hash, 36);
      }
    }
    return name;
  }

  /**
   * Mysql-specific: Locks all tables for triggers that have to be updated.
   */
  public void lockTables(DdlBuffer buffer, Collection<String> tables) throws IOException {

  }

  /**
   * Mysql-specific: Unlocks all tables for triggers that have to be updated.
   */
  public void unlockTables(DdlBuffer buffer, Collection<String> tables) throws IOException {

  }

  public boolean suppressPrimaryKeyOnPartition() {
    return false;
  }

  public void addTablePartition(DdlBuffer apply, String partitionMode, String partitionColumn) throws IOException {
    // only supported by postgres initially
  }
}
