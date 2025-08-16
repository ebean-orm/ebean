package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.Platform;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbDefaultValue;
import io.ebean.config.dbplatform.DbIdentity;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.*;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.VowelRemover;
import io.ebeaninternal.dbmigration.migration.*;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controls the DDL generation for a specific database platform.
 */
public class PlatformDdl {

  // matches on pattern "check ( COLUMNAME ... )". ColumnName is match group 2;
  private static final Pattern CHECK_PATTERN = Pattern.compile("(.*?\\( *)([^ ]+)(.*)");

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
  protected String identityStartWith = "start with";
  protected String identityIncrementBy = "increment by";
  protected String identityCache = "cache";
  protected String sequenceStartWith = "start with";
  protected String sequenceIncrementBy = "increment by";
  protected String sequenceCache = "cache";
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
  protected String columnNotNull = "not null";
  protected String updateNullWithDefault = "update ${table} set ${column} = ${default} where ${column} is null";
  protected boolean createSchemaSupport;
  protected String createSchema = "create schema if not exists";
  protected String createTable = "create table";
  protected String dropColumn = "drop column";
  protected String addForeignKeySkipCheck = "";
  protected String uniqueIndex = "unique";
  protected String indexConcurrent = "";
  protected String createIndexIfNotExists = "";
  /**
   * Set false for MsSqlServer to allow multiple nulls for OneToOne mapping.
   */
  protected boolean inlineUniqueWhenNullable = true;
  protected DbConstraintNaming naming;
  /**
   * Generally not desired as then they are not named (used with SQLite).
   */
  protected boolean inlineForeignKeys;
  protected boolean includeStorageEngine;
  protected final DbDefaultValue dbDefaultValue;
  protected String fallbackArrayType = "varchar(1000)";

  public PlatformDdl(DatabasePlatform platform) {
    this.platform = platform;
    this.dbIdentity = platform.dbIdentity();
    this.dbDefaultValue = platform.dbDefaultValue();
    this.typeConverter = new PlatformTypeConverter(platform.dbTypeMap());
  }

  /**
   * Set configuration options.
   */
  public void configure(DatabaseBuilder.Settings config) {
    historyDdl.configure(config, this);
    naming = config.getConstraintNaming();
  }

  /**
   * Create a DdlHandler for the specific database platform.
   */
  public DdlHandler createDdlHandler(DatabaseBuilder.Settings config) {
    return new BaseDdlHandler(config, this);
  }

  /**
   * Return the identity type to use given the support in the underlying database
   * platform for sequences and identity/autoincrement.
   */
  public IdType useIdentityType(IdType modelIdentity) {
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
  private IdType identityType(IdType modelIdentity, IdType platformIdType, boolean supportsSequence, boolean supportsIdentity) {
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
  public String asIdentityColumn(String columnDefn, DdlIdentity identity) {
    return columnDefn + identitySuffix;
  }

  /**
   * SQl2003 standard identity definition.
   */
  protected String asIdentityStandardOptions(String columnDefn, DdlIdentity identity) {
    StringBuilder sb = new StringBuilder(columnDefn.length() + 60);
    sb.append(columnDefn).append(identity.optionGenerated());
    sb.append(identity.identityOptions(identityStartWith, identityIncrementBy, identityCache));
    return sb.toString();
  }

  /**
   * Return true if the table and column comments are included inline.
   */
  public boolean isInlineComments() {
    return inlineComments;
  }

  /**
   * Return true if the platform includes storage engine clause.
   */
  public boolean isIncludeStorageEngine() {
    return includeStorageEngine;
  }

  /**
   * Return true if foreign key reference constraints need to inlined with create table.
   * Ideally we don't do this as then the constraints are not named. Do this for SQLite.
   */
  public boolean isInlineForeignKeys() {
    return inlineForeignKeys;
  }

  /**
   * By default this does nothing returning null / no lock timeout.
   */
  public String setLockTimeout(int lockTimeoutSeconds) {
    return null;
  }

  /**
   * Write all the table columns converting to platform types as necessary.
   */
  public void writeTableColumns(DdlBuffer apply, List<Column> columns, DdlIdentity identity) {
    if ("true".equalsIgnoreCase(System.getProperty("ebean.ddl.sortColumns", "true"))) {
      columns = sortColumns(columns);
    }
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        apply.append(",");
      }
      apply.newLine();
      writeColumnDefinition(apply, columns.get(i), identity);
    }

    for (Column column : columns) {
      String checkConstraint = column.getCheckConstraint();
      if (hasValue(checkConstraint)) {
        checkConstraint = createCheckConstraint(maxConstraintName(column.getCheckConstraintName()), checkConstraint);
        if (hasValue(checkConstraint)) {
          apply.append(",").newLine();
          apply.append(checkConstraint);
        }
      }
    }
  }

  protected List<Column> sortColumns(List<Column> columns) {
    // do nothing by default
    return columns;
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, Column column, DdlIdentity identity) {
    buffer.append("  ");
    buffer.append(quote(column.getName()), 29);

    String columnDefn = columnDefn(column);
    if (identity.useIdentity() && isTrue(column.isPrimaryKey())) {
      columnDefn = asIdentityColumn(columnDefn, identity);
    }
    buffer.append(columnDefn);
    if (!Boolean.TRUE.equals(column.isPrimaryKey())) {
      String defaultValue = convertDefaultValue(column.getDefaultValue());
      if (defaultValue != null) {
        buffer.append(" default ").append(defaultValue);
      }
    }
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      buffer.appendWithSpace(columnNotNull);
    }
    // add check constraints later as we really want to give them a nice name
    // so that the database can potentially provide a nice SQL error
  }

  /**
   * Return the plaform specific column type definition.
   */
  protected String columnDefn(Column column) {
    return convert(column.getType());
  }

  /**
   * Returns the check constraint.
   */
  public String createCheckConstraint(String ckName, String checkConstraint) {
    return "  constraint " + maxConstraintName(ckName) + " " + quoteCheckConstraint(checkConstraint);
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
    return "alter table " + alterTableIfExists + quote(tableName) + " " + dropConstraintIfExists + " " + maxConstraintName(fkName);
  }

  /**
   * Convert the standard type to the platform specific type.
   */
  public String convert(String type) {
    if (type == null) {
      return null;
    }
    String[] tmp = type.split(";", -1); // do not discard trailing empty strings
    int index = extract(tmp);
    if (index < tmp.length - 1) {
      // this is a platform specific definition. So do not apply further conversions
      return tmp[index];
    }
    type = tmp[index];
    if (type.contains("[]")) {
      return convertArrayType(type);
    }
    return typeConverter.convert(type);
  }

  // if columnType is different for different platforms, use pattern
  // @Column(columnDefinition = PLATFORM1;DEFINITION1;PLATFORM2;DEFINITON2;DEFINITON-DEFAULT)
  // e.g. @Column(columnDefinition = "db2;blob(64M);sqlserver,h2;varchar(227);varchar(127)")
  protected String extract(String type) {
    if (type == null) {
      return null;
    }
    String[] tmp = type.split(";", -1); // do not discard trailing empty strings
    return tmp[extract(tmp)]; // else
  }

  protected int extract(String[] types) {
    if (types.length % 2 == 0) {
      throw new IllegalArgumentException("You need an odd number of arguments in '" + String.join(";", types) + "'. See Issue #2559 for details");
    }
    for (int i = 0; i < types.length - 2; i += 2) {
      String[] platforms = types[i].split(",");
      for (String plat : platforms) {
        if (platform.isPlatform(Platform.valueOf(plat.toUpperCase(Locale.ENGLISH)))) {
          return i + 1;
        }
      }
    }
    return types.length - 1; // else
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
  public void createWithHistory(DdlWrite writer, MTable table) {
    historyDdl.createWithHistory(writer, table);
  }

  /**
   * Drop history support for a given table.
   */
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    historyDdl.dropHistoryTable(writer, dropHistoryTable);
  }

  /**
   * Add history support to an existing table.
   */
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {
    historyDdl.addHistoryTable(writer, addHistoryTable);
  }

  /**
   * Regenerate the history triggers (or function) due to a column being added/dropped/excluded or included.
   */
  public void regenerateHistoryTriggers(DdlWrite writer, String tableName) {
    historyDdl.updateTriggers(writer, tableName);
  }

  /**
   * Generate and return the create sequence DDL.
   */
  public String createSequence(String sequenceName, DdlIdentity identity) {
    StringBuilder sb = new StringBuilder("create sequence ");
    sb.append(quote(sequenceName));
    sb.append(identity.sequenceOptions(sequenceStartWith, sequenceIncrementBy, sequenceCache));
    sb.append(';');
    return sb.toString();
  }

  /**
   * Return the drop sequence statement (potentially with if exists clause).
   */
  public String dropSequence(String sequenceName) {
    return dropSequenceIfExists + quote(sequenceName);
  }

  /**
   * Return the drop table statement (potentially with if exists clause).
   */
  public String dropTable(String tableName) {
    return dropTableIfExists + quote(tableName) + dropTableCascade;
  }

  /**
   * Return the drop index statement for known non concurrent index.
   */
  public String dropIndex(String indexName, String tableName) {
    return dropIndex(indexName, tableName, false);
  }

  /**
   * Return the drop index statement.
   */
  public String dropIndex(String indexName, String tableName, boolean concurrent) {
    return dropIndexIfExists + maxConstraintName(indexName);
  }

  public String createIndex(WriteCreateIndex create) {
    if (create.useDefinition()) {
      return create.getDefinition();
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append("create ");
    if (create.isUnique()) {
      buffer.append(uniqueIndex).append(" ");
    }
    buffer.append("index ");
    if (create.isConcurrent()) {
      buffer.append(indexConcurrent);
    }
    if (create.isNotExistsCheck()) {
      buffer.append(createIndexIfNotExists);
    }
    buffer.append(maxConstraintName(create.getIndexName())).append(" on ").append(quote(create.getTableName()));
    appendColumns(create.getColumns(), buffer);
    return buffer.toString();
  }

  /**
   * Return the foreign key constraint when used inline with create table.
   */
  public String tableInlineForeignKey(WriteForeignKey request) {
    StringBuilder buffer = new StringBuilder(90);
    buffer.append("foreign key");
    appendColumns(request.cols(), buffer);
    buffer.append(" references ").append(quote(request.refTable()));
    appendColumns(request.refCols(), buffer);
    appendForeignKeySuffix(request, buffer);
    return buffer.toString();
  }

  /**
   * Add foreign key.
   */
  public String alterTableAddForeignKey(DdlOptions options, WriteForeignKey request) {
    StringBuilder buffer = new StringBuilder(90);
    buffer
      .append("alter table ").append(quote(request.table()))
      .append(" add constraint ").append(maxConstraintName(request.fkName()))
      .append(" foreign key");
    appendColumns(request.cols(), buffer);
    buffer
      .append(" references ")
      .append(quote(request.refTable()));
    appendColumns(request.refCols(), buffer);
    appendForeignKeySuffix(request, buffer);
    if (options.isForeignKeySkipCheck()) {
      buffer.append(addForeignKeySkipCheck);
    }
    return buffer.toString();
  }

  protected void appendForeignKeySuffix(WriteForeignKey request, StringBuilder buffer) {
    appendForeignKeyOnDelete(buffer, withDefault(request.onDelete()));
    appendForeignKeyOnUpdate(buffer, withDefault(request.onUpdate()));
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
    switch (mode) {
      case SET_NULL:
        return "set null";
      case SET_DEFAULT:
        return "set default";
      case RESTRICT:
        return "restrict";
      case CASCADE:
        return "cascade";
      default:
        throw new IllegalStateException("Unknown mode " + mode);
    }
  }

  /**
   * Drop a unique constraint from the table (Sometimes this is an index).
   */
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    return "alter table " + quote(tableName) + " " + dropUniqueConstraint + " " + maxConstraintName(uniqueConstraintName);
  }

  /**
   * Drop a unique constraint from the table.
   */
  public String alterTableDropConstraint(String tableName, String constraintName) {
    return "alter table " + quote(tableName) + " " + dropConstraintIfExists + " " + maxConstraintName(constraintName);
  }

  /**
   * Add a unique constraint to the table.
   * <p>
   * Overridden by MsSqlServer for specific null handling on unique constraints.
   */
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, String[] nullableColumns) {
    StringBuilder buffer = new StringBuilder(90);
    buffer.append("alter table ").append(quote(tableName)).append(" add constraint ").append(maxConstraintName(uqName)).append(" unique ");
    appendColumns(columns, buffer);
    return buffer.toString();
  }

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
    }

  }

  /**
   * This method is used from DbTriggerBasedHistoryDdl to add the sysPeriodColumns.
   */
  public void alterTableAddColumn(DdlWrite writer, String tableName, String columnName, String columnType, String defaultValue) {
    String convertedType = convert(columnType);
    DdlBuffer buffer = alterTable(writer, tableName).append(addColumn, columnName);
    buffer.append(convertedType);

    if (defaultValue != null) {
      buffer.append(" default ");
      buffer.append(defaultValue);
    }
  }

  public void alterTableDropColumn(DdlWrite writer, String tableName, String columnName) {
    alterTable(writer, tableName).append(dropColumn, columnName);
  }

  /**
   * Return true if unique constraints for nullable columns can be inlined as normal.
   * Returns false for MsSqlServer and DB2 due to it's not possible to to put a constraint
   * on a nullable column
   */
  public boolean isInlineUniqueWhenNullable() {
    return inlineUniqueWhenNullable;
  }

  /**
   * Alter a column type.
   * <p>
   * Note that that MySql, SQL Server, and HANA instead use alterColumn()
   * </p>
   */
  protected void alterColumnType(DdlWrite writer, AlterColumn alter) {
    alterTable(writer, alter.getTableName()).append(alterColumn, alter.getColumnName())
      .append(columnSetType).append(convert(alter.getType()));
  }

  /**
   * Alter a column adding or removing the not null constraint.
   * <p>
   * Note that that MySql, SQL Server, and HANA instead use alterColumn()
   * </p>
   */
  protected void alterColumnNotnull(DdlWrite writer, AlterColumn alter) {
    DdlBuffer buffer = alterTable(writer, alter.getTableName()).append(alterColumn, alter.getColumnName());
    if (Boolean.TRUE.equals(alter.isNotnull())) {
      buffer.append(columnSetNotnull);
    } else {
      buffer.append(columnSetNull);
    }
  }

  /**
   * Alter table adding the check constraint.
   */
  public String alterTableAddCheckConstraint(String tableName, String checkConstraintName, String checkConstraint) {
    return "alter table " + quote(tableName) + " " + addConstraint + " " + maxConstraintName(checkConstraintName) + " " + quoteCheckConstraint(checkConstraint);
  }


  /**
   * Alter column setting the default value.
   * <p>
   * Note that that MySql, SQL Server, and HANA instead use alterColumn()
   * </p>
   */
  protected void alterColumnDefault(DdlWrite writer, AlterColumn alter) {
    DdlBuffer buffer = alterTable(writer, alter.getTableName()).append(alterColumn, alter.getColumnName());
    if (DdlHelp.isDropDefault(alter.getDefaultValue())) {
      buffer.append(columnDropDefault);
    } else {
      buffer.append(columnSetDefault).appendWithSpace(convertDefaultValue(alter.getDefaultValue()));
    }
  }

  /**
   * Alter column setting (type, default value and not null constraint).
   * <p>
   * Used by MySql, SQL Server, and HANA as these require all column attributes to
   * be set together.
   */
  public void alterColumn(DdlWrite writer, AlterColumn alter) {
    if (hasValue(alter.getType())) {
      alterColumnType(writer, alter);
    }
    if (hasValue(alter.getDefaultValue())) {
      alterColumnDefault(writer, alter);
    }
    if (alter.isNotnull() != null) {
      alterColumnNotnull(writer, alter);
    }
  }

  /**
   * Creates or replace a new DdlAlterTable for given tableName.
   */
  protected DdlAlterTable alterTable(DdlWrite writer, String tableName) {
    return writer.applyAlterTable(tableName, k -> new BaseAlterTableWrite(k, this));
  }

  protected void appendColumns(String[] columns, StringBuilder buffer) {
    buffer.append(" (");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(quote(columns[i].trim()));
    }
    buffer.append(")");
  }

  public DatabasePlatform getPlatform() {
    return platform;
  }

  public String getUpdateNullWithDefault() {
    return updateNullWithDefault;
  }

  /**
   * Return true if null or trimmed string is empty.
   */
  protected boolean hasValue(String value) {
    return value != null && !value.trim().isEmpty();
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
  public void inlineTableComment(DdlBuffer apply, String tableComment) {
    // do nothing by default (MySql only)
  }

  /**
   * Add an table storage engine to the create table statement.
   */
  public void tableStorageEngine(DdlBuffer apply, String storageEngine) {
    // do nothing by default
  }

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    if (DdlHelp.isDropComment(tableComment)) {
      tableComment = "";
    }
    apply.append(String.format("comment on table %s is '%s'", quote(tableName), tableComment)).endOfStatement();
  }

  /**
   * Add column comment as a separate statement.
   */
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    if (DdlHelp.isDropComment(comment)) {
      comment = "";
    }
    apply.append(String.format("comment on column %s.%s is '%s'", quote(table), quote(column), comment)).endOfStatement();
  }

  /**
   * Use this to generate a prolog for each script (stored procedures)
   */
  public void generateProlog(DdlWrite writer) {
    // do nothing by default
  }

  /**
   * Use this to generate an epilog. Will be added at the end of script
   */
  public void generateEpilog(DdlWrite writer) {
    // do nothing by default
  }

  /**
   * Shortens the given name to the maximum constraint name length of the platform in a deterministic way.
   * <p>
   * First, all vowels are removed, If the string is still to long, 31 bits are taken from the hash code
   * of the string and base36 encoded (10 digits and 26 chars) string.
   * <p>
   * As 36^6 > 31^2, the resulting string is never longer as 6 chars.
   */
  protected String maxConstraintName(String name) {
    if (name.length() > platform.maxConstraintNameLength()) {
      int hash = name.hashCode() & 0x7FFFFFFF;
      name = VowelRemover.trim(name, 4);
      if (name.length() > platform.maxConstraintNameLength()) {
        return name.substring(0, platform.maxConstraintNameLength() - 7) + "_" + Integer.toString(hash, 36);
      }
    }
    return name;
  }

  /**
   * Returns the database-specific "create table" command prefix. For HANA this is
   * either "create column table" or "create row table", for all other databases
   * it is "create table".
   *
   * @return The "create table" command prefix
   */
  public String getCreateTableCommandPrefix() {
    return createTable;
  }

  public void addTablePartition(DdlBuffer apply, String partitionMode, String partitionColumn) {
    // only supported by postgres and yugabyte
  }

  public void addDefaultTablePartition(DdlBuffer apply, String tableName) {
    // only supported by postgres and yugabyte
  }

  /**
   * Adds tablespace declaration. Now only supported for db2.
   */
  public void addTablespace(DdlBuffer apply, String tablespaceName, String indexTablespace, String lobTablespace) {
    throw new UnsupportedOperationException("Tablespaces are not supported for this platform");
  }

  /**
   * Moves the table to an other tablespace.
   */
  public String alterTableTablespace(String tablename, String tableSpace, String indexSpace, String lobSpace) {
    if (tableSpace != null || indexSpace != null || lobSpace != null) {
      throw new UnsupportedOperationException("Tablespaces are not supported for this platform");
    }
    return null;
  }

  protected String quote(String dbName) {
    return platform.convertQuotedIdentifiers(dbName);
  }

  protected String quoteCheckConstraint(String checkConstraint) {
    Matcher matcher = CHECK_PATTERN.matcher(checkConstraint);
    if (matcher.matches()) {
      return matcher.replaceFirst("$1" + quote(matcher.group(2)) + "$3");
    }
    return checkConstraint;
  }

  public void createSchema(DdlWrite writer, CreateSchema request) {
    DdlBuffer apply = writer.apply();
    if (!createSchemaSupport) {
      apply.append("-- ");
    }
    apply.append(createSchema).append(" ").append(quote(request.getName())).endOfStatement();
  }

  /**
   * Override to return true for Postgres / platforms that want the partition column to be
   * part of the primary key when using table partitioning.
   */
  public boolean addPartitionColumnToPrimaryKey() {
    return false;
  }
}
