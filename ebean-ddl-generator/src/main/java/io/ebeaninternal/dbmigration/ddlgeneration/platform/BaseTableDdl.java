package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.DbHistorySupport;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlOptions;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.TableDdl;
import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DdlScript;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;
import io.ebeaninternal.dbmigration.migration.ForeignKey;
import io.ebeaninternal.dbmigration.migration.UniqueConstraint;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.MTableIdentity;
import io.ebeaninternal.server.deploy.IdentityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ebean.util.StringHelper.replace;
import static io.ebeaninternal.api.PlatformMatch.matchPlatform;
import static io.ebeaninternal.dbmigration.ddlgeneration.platform.SplitColumns.split;

/**
 * Base implementation for 'create table' and 'alter table' statements.
 */
public class BaseTableDdl implements TableDdl {

  enum HistorySupport {
    NONE,
    SQL2011,
    TRIGGER_BASED
  }

  protected final DbConstraintNaming naming;

  protected final NamingConvention namingConvention;

  protected final PlatformDdl platformDdl;

  protected final String historyTableSuffix;

  /**
   * Used when unique constraints specifically for OneToOne can't be created normally (MsSqlServer).
   */
  protected final List<Column> externalUnique = new ArrayList<>();

  protected final List<UniqueConstraint> externalCompoundUnique = new ArrayList<>();

  /**
   * Base tables that have associated history tables that need their triggers/functions regenerated as
   * columns have been added, removed, included or excluded.
   */
  protected final Map<String, HistoryTableUpdate> regenerateHistoryTriggers = new LinkedHashMap<>();

  private final boolean strictMode;

  private final HistorySupport historySupport;

  /**
   * Helper class that is used to execute the migration ddl before and after the migration action.
   */
  private class DdlMigrationHelp {
    private final List<String> before;
    private final List<String> after;
    private final String tableName;
    private final String columnName;
    private final String defaultValue;
    private final boolean withHistory;

    /**
     * Constructor for DdlMigrationHelp when adding a NEW column.
     */
    DdlMigrationHelp(String tableName, Column column, boolean withHistory) {
      this.tableName = tableName;
      this.columnName = column.getName();
      this.defaultValue = platformDdl.convertDefaultValue(column.getDefaultValue());
      boolean alterNotNull = Boolean.TRUE.equals(column.isNotnull());
      if (column.getBefore().isEmpty() && alterNotNull && defaultValue == null) {
        handleStrictError(tableName, columnName);
      }
      before = getScriptsForPlatform(column.getBefore());
      after = getScriptsForPlatform(column.getAfter());
      this.withHistory = withHistory;
    }

    /**
     * Constructor for DdlMigrationHelp when altering a column.
     */
    DdlMigrationHelp(AlterColumn alter) {
      this.tableName = alter.getTableName();
      this.columnName = alter.getColumnName();
      String tmp = alter.getDefaultValue() != null ? alter.getDefaultValue() : alter.getCurrentDefaultValue();
      this.defaultValue = platformDdl.convertDefaultValue(tmp);
      boolean alterNotNull = Boolean.TRUE.equals(alter.isNotnull());
      // here we add the platform's default update script
      withHistory = isTrue(alter.isWithHistory());
      if (alter.getBefore().isEmpty() && alterNotNull) {
        if (defaultValue == null) {
          handleStrictError(tableName, columnName);
        }
        before = Collections.singletonList(platformDdl.getUpdateNullWithDefault());
      } else {
        before = getScriptsForPlatform(alter.getBefore());
      }
      after = getScriptsForPlatform(alter.getAfter());
    }

    void writeBefore(DdlBuffer buffer) {
      if (!before.isEmpty()) {
        buffer.end();
      }

      if (!before.isEmpty() && withHistory) {
        buffer.append("-- NOTE: table has @History - special migration may be necessary").newLine();
      }
      for (String ddlScript : before) {
        buffer.appendStatement(translate(ddlScript, tableName, columnName, defaultValue));
      }
    }

    void writeAfter(DdlBuffer buffer) {
      if (!after.isEmpty() && withHistory) {
        buffer.append("-- NOTE: table has @History - special migration may be necessary").newLine();
      }
      // here we run post migration scripts
      for (String ddlScript : after) {
        buffer.appendStatement(translate(ddlScript, tableName, columnName, defaultValue));
      }
      if (!after.isEmpty()) {
        buffer.end();
      }
    }

    private List<String> getScriptsForPlatform(List<DdlScript> scripts) {
      Platform searchPlatform = platformDdl.getPlatform().getPlatform();
      for (DdlScript script : scripts) {
        if (matchPlatform(searchPlatform, script.getPlatforms())) {
          // just returns the first match (rather than appends them)
          return script.getDdl();
        }
      }
      return Collections.emptyList();
    }

    /**
     * Replaces Table name (${table}), Column name (${column}) and default value (${default}) in DDL.
     */
    private String translate(String ddl, String tableName, String columnName, String defaultValue) {
      String ret = replace(ddl, "${table}", tableName);
      ret = replace(ret, "${column}", columnName);
      return replace(ret, "${default}", defaultValue);
    }

    private void handleStrictError(String tableName, String columnName) {
      if (strictMode) {
        String message = "DB Migration of non-null column with no default value specified for: " + tableName + "." + columnName+" Use @DbDefault to specify a default value or specify dbMigration.setStrictMode(false)";
        throw new IllegalArgumentException(message);
      }
    }

    public String getDefaultValue() {
      return defaultValue;
    }
  }

  /**
   * Construct with a naming convention and platform specific DDL.
   */
  public BaseTableDdl(DatabaseConfig config, PlatformDdl platformDdl) {
    this.namingConvention = config.getNamingConvention();
    this.naming = config.getConstraintNaming();
    this.historyTableSuffix = config.getHistoryTableSuffix();
    this.platformDdl = platformDdl;
    this.platformDdl.configure(config);
    this.strictMode = config.isDdlStrictMode();
    DbHistorySupport hist = platformDdl.getPlatform().getHistorySupport();
    if (hist == null) {
      this.historySupport = HistorySupport.NONE;
    } else {
      this.historySupport = hist.isStandardsBased() ? HistorySupport.SQL2011 : HistorySupport.TRIGGER_BASED;
    }
  }

  /**
   * Reset counters and index set for each table processed.
   */
  protected void reset() {
    externalUnique.clear();
    externalCompoundUnique.clear();
  }

  /**
   * Generate the appropriate 'create table' and matching 'drop table' statements
   * and add them to the appropriate 'apply' and 'rollback' buffers.
   */
  @Override
  public void generate(DdlWrite writer, CreateTable createTable) {
    reset();

    String tableName = lowerTableName(createTable.getName());
    List<Column> columns = createTable.getColumn();
    List<Column> pk = determinePrimaryKeyColumns(columns);

    DdlIdentity identity = DdlIdentity.NONE;
    if ((pk.size() == 1)) {
      final IdentityMode identityMode = MTableIdentity.fromCreateTable(createTable);
      IdType idType = platformDdl.useIdentityType(identityMode.getIdType());
      String sequenceName = identityMode.getSequenceName();
      if (IdType.SEQUENCE == idType && (sequenceName == null || sequenceName.isEmpty())) {
        sequenceName = sequenceName(createTable, pk);
      }
      identity = new DdlIdentity(idType, identityMode, sequenceName);
    }

    String partitionMode = createTable.getPartitionMode();

    DdlBuffer apply = writer.apply();
    apply.append(platformDdl.getCreateTableCommandPrefix()).append(" ").append(tableName).append(" (");
    writeTableColumns(apply, columns, identity);
    writeUniqueConstraints(apply, createTable);
    writeCompoundUniqueConstraints(apply, createTable);
    if (!pk.isEmpty()) {
      // defined on the columns
      if (partitionMode == null || !platformDdl.suppressPrimaryKeyOnPartition()) {
        writePrimaryKeyConstraint(apply, createTable.getPkName(), toColumnNames(pk));
      }
    }
    if (platformDdl.isInlineForeignKeys()) {
      writeInlineForeignKeys(writer, createTable);
    }
    apply.newLine().append(")");
    addTableStorageEngine(apply, createTable);
    addTableCommentInline(apply, createTable);
    if (partitionMode != null) {
      platformDdl.addTablePartition(apply, partitionMode, createTable.getPartitionColumn());
    }
    apply.endOfStatement();

    addComments(apply, createTable);
    writeUniqueOneToOneConstraints(writer, createTable);
    if (isTrue(createTable.isWithHistory())) {
      // create history with rollback before the
      // associated drop table is written to rollback
      createWithHistory(writer, createTable.getName());
    }

    // add drop table to the rollback buffer - do this before
    // we drop the related sequence (if sequences are used)
    dropTable(writer.dropAll(), tableName);

    if (identity.useSequence()) {
      writeSequence(writer, identity);
    }

    // add blank line for a bit of whitespace between tables
    apply.end();
    writer.dropAll().end();
    if (!platformDdl.isInlineForeignKeys()) {
      writeAddForeignKeys(writer, createTable);
    }
  }

  private String sequenceName(CreateTable createTable, List<Column> pk) {
    return namingConvention.getSequenceName(createTable.getName(), pk.get(0).getName());
  }

  /**
   * Add table and column comments (separate from the create table statement).
   */
  private void addComments(DdlBuffer apply, CreateTable createTable) {
    if (!platformDdl.isInlineComments()) {
      String tableComment = createTable.getComment();
      if (hasValue(tableComment)) {
        platformDdl.addTableComment(apply, createTable.getName(), tableComment);
      }
      for (Column column : createTable.getColumn()) {
        if (!StringHelper.isNull(column.getComment())) {
          platformDdl.addColumnComment(apply, createTable.getName(), column.getName(), column.getComment());
        }
      }
    }
  }

  /**
   * Add the table storage engine clause.
   */
  private void addTableStorageEngine(DdlBuffer apply, CreateTable createTable) {
    if (platformDdl.isIncludeStorageEngine()) {
      platformDdl.tableStorageEngine(apply, createTable.getStorageEngine());
    }
  }

  /**
   * Add the table comment inline with the create table statement.
   */
  private void addTableCommentInline(DdlBuffer apply, CreateTable createTable) {
    if (platformDdl.isInlineComments()) {
      String tableComment = createTable.getComment();
      if (!StringHelper.isNull(tableComment)) {
        platformDdl.inlineTableComment(apply, tableComment);
      }
    }
  }

  private void writeTableColumns(DdlBuffer apply, List<Column> columns, DdlIdentity identity) {
    platformDdl.writeTableColumns(apply, columns, identity);
  }

  /**
   * Specific handling of OneToOne unique constraints for MsSqlServer.
   * For all other DB platforms these unique constraints are done inline as per normal.
   */
  protected void writeUniqueOneToOneConstraints(DdlWrite writer, CreateTable createTable) {
    String tableName = createTable.getName();
    for (Column col : externalUnique) {
      String uqName = col.getUniqueOneToOne();
      if (uqName == null) {
        uqName = col.getUnique();
      }
      String[] columnNames = {col.getName()};
      writer.apply().appendStatement(platformDdl.alterTableAddUniqueConstraint(tableName, uqName, columnNames, Boolean.TRUE.equals(col.isNotnull()) ? null : columnNames));
      writer.dropAllForeignKeys().appendStatement(platformDdl.dropIndex(uqName, tableName));
    }

    for (UniqueConstraint constraint : externalCompoundUnique) {
      String uqName = constraint.getName();
      String[] columnNames = split(constraint.getColumnNames());
      String[] nullableColumns = split(constraint.getNullableColumns());

      writer.apply().appendStatement(platformDdl.alterTableAddUniqueConstraint(tableName, uqName, columnNames, nullableColumns));
      writer.dropAllForeignKeys().appendStatement(platformDdl.dropIndex(uqName, tableName));
    }
  }

  protected void writeSequence(DdlWrite writer, DdlIdentity identity) {
    String seqName = identity.getSequenceName();
    String createSeq = platformDdl.createSequence(seqName, identity);
    if (hasValue(createSeq)) {
      writer.apply().append(createSeq).newLine();
      writer.dropAll().appendStatement(platformDdl.dropSequence(seqName));
    }
  }

  protected void createWithHistory(DdlWrite writer, String name) {
    MTable table = writer.getTable(name);
    platformDdl.createWithHistory(writer, table);
  }

  protected void writeInlineForeignKeys(DdlWrite writer, CreateTable createTable) {
    for (Column column : createTable.getColumn()) {
      String references = column.getReferences();
      if (hasValue(references)) {
        writeInlineForeignKey(writer, column);
      }
    }
    writeInlineCompoundForeignKeys(writer, createTable);
  }

  protected void writeInlineForeignKey(DdlWrite writer, Column column) {
    String fkConstraint = platformDdl.tableInlineForeignKey(new WriteForeignKey(null, column));
    writer.apply().append(",").newLine().append("  ").append(fkConstraint);
  }

  protected void writeInlineCompoundForeignKeys(DdlWrite writer, CreateTable createTable) {
    for (ForeignKey key : createTable.getForeignKey()) {
      String fkConstraint = platformDdl.tableInlineForeignKey(new WriteForeignKey(null, key));
      writer.apply().append(",").newLine().append("  ").append(fkConstraint);
    }
  }

  protected void writeAddForeignKeys(DdlWrite writer, CreateTable createTable) {
    for (Column column : createTable.getColumn()) {
      String references = column.getReferences();
      if (hasValue(references)) {
        writeForeignKey(writer, createTable.getName(), column);
      }
    }
    writeAddCompoundForeignKeys(writer, createTable);
  }

  protected void writeAddCompoundForeignKeys(DdlWrite writer, CreateTable createTable) {
    for (ForeignKey key : createTable.getForeignKey()) {
      writeForeignKey(writer, new WriteForeignKey(createTable.getName(), key));
    }
  }

  protected void writeForeignKey(DdlWrite writer, String tableName, Column column) {
    writeForeignKey(writer, new WriteForeignKey(tableName, column));
  }

  protected void writeForeignKey(DdlWrite writer, WriteForeignKey request) {
    DdlBuffer fkeyBuffer = writer.applyForeignKeys();
    String tableName = lowerTableName(request.table());
    if (request.indexName() != null) {
      // no matching unique constraint so add the index
      fkeyBuffer.appendStatement(platformDdl.createIndex(new WriteCreateIndex(request.indexName(), tableName, request.cols(), false)));
    }
    alterTableAddForeignKey(writer.getOptions(), fkeyBuffer, request);
    fkeyBuffer.end();

    writer.dropAllForeignKeys().appendStatement(platformDdl.alterTableDropForeignKey(tableName, request.fkName()));
    if (hasValue(request.indexName())) {
      writer.dropAllForeignKeys().appendStatement(platformDdl.dropIndex(request.indexName(), tableName));
    }
    writer.dropAllForeignKeys().end();
  }

  protected void alterTableAddForeignKey(DdlOptions options, DdlBuffer buffer, WriteForeignKey request) {
    buffer.appendStatement(platformDdl.alterTableAddForeignKey(options, request));
  }

  protected void appendColumns(String[] columns, DdlBuffer buffer) {
    buffer.append(" (");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(lowerColumnName(columns[i].trim()));
    }
    buffer.append(")");
  }

  /**
   * Add 'drop table' statement to the buffer.
   */
  protected void dropTable(DdlBuffer buffer, String tableName) {
    buffer.appendStatement(platformDdl.dropTable(tableName));
  }

  /**
   * Add 'drop sequence' statement to the buffer.
   */
  protected void dropSequence(DdlBuffer buffer, String sequenceName) {
    buffer.appendStatement(platformDdl.dropSequence(sequenceName));
  }

  protected void writeCompoundUniqueConstraints(DdlBuffer apply, CreateTable createTable) {
    boolean inlineUniqueWhenNull = platformDdl.isInlineUniqueWhenNullable();
    for (UniqueConstraint uniqueConstraint : createTable.getUniqueConstraint()) {
      if (platformInclude(uniqueConstraint.getPlatforms())) {
        if (inlineUniqueWhenNull) {
          String uqName = uniqueConstraint.getName();
          apply.append(",").newLine();
          apply.append("  constraint ").append(uqName).append(" unique");
          appendColumns(split(uniqueConstraint.getColumnNames()), apply);
        } else {
          externalCompoundUnique.add(uniqueConstraint);
        }
      }
    }
  }

  private boolean platformInclude(String platforms) {
    return matchPlatform(platformDdl.getPlatform().getPlatform(), platforms);
  }

  /**
   * Write the unique constraints inline with the create table statement.
   */
  protected void writeUniqueConstraints(DdlBuffer apply, CreateTable createTable) {
    boolean inlineUniqueWhenNullable = platformDdl.isInlineUniqueWhenNullable();
    List<Column> columns = new WriteUniqueConstraint(createTable.getColumn()).uniqueKeys();
    for (Column column : columns) {
      if (Boolean.TRUE.equals(column.isNotnull()) || inlineUniqueWhenNullable) {
        // normal mechanism for adding unique constraint
        inlineUniqueConstraintSingle(apply, column);
      } else {
        // SqlServer & DB2 specific mechanism for adding unique constraints (that allow nulls)
        externalUnique.add(column);
      }
    }
  }

  /**
   * Write the unique constraint inline with the create table statement.
   */
  protected void inlineUniqueConstraintSingle(DdlBuffer buffer, Column column) {
    String uqName = column.getUnique();
    if (uqName == null) {
      uqName = column.getUniqueOneToOne();
    }
    buffer.append(",").newLine();
    buffer.append("  constraint ").append(uqName).append(" unique ");
    buffer.append("(");
    buffer.append(lowerColumnName(column.getName()));
    buffer.append(")");
  }

  /**
   * Write the primary key constraint inline with the create table statement.
   */
  protected void writePrimaryKeyConstraint(DdlBuffer buffer, String pkName, String[] pkColumns) {
    buffer.append(",").newLine();
    buffer.append("  constraint ").append(pkName).append(" primary key");
    appendColumns(pkColumns, buffer);
  }

  /**
   * Return as an array of string column names.
   */
  protected String[] toColumnNames(List<Column> columns) {
    String[] cols = new String[columns.size()];
    for (int i = 0; i < cols.length; i++) {
      cols[i] = columns.get(i).getName();
    }
    return cols;
  }

  /**
   * Convert the table lower case.
   */
  protected String lowerTableName(String name) {
    return naming.lowerTableName(name);
  }

  /**
   * Convert the column name to lower case.
   */
  protected String lowerColumnName(String name) {
    return naming.lowerColumnName(name);
  }

  /**
   * Return the list of columns that make the primary key.
   */
  protected List<Column> determinePrimaryKeyColumns(List<Column> columns) {
    List<Column> pk = new ArrayList<>(3);
    for (Column column : columns) {
      if (isTrue(column.isPrimaryKey())) {
        pk.add(column);
      }
    }
    return pk;
  }

  @Override
  public void generate(DdlWrite writer, CreateIndex index) {
    if (platformInclude(index.getPlatforms())) {
      writer.apply().appendStatement(platformDdl.createIndex(new WriteCreateIndex(index)));
      writer.dropAll().appendStatement(platformDdl.dropIndex(index.getIndexName(), index.getTableName(), Boolean.TRUE.equals(index.isConcurrent())));
    }
  }

  @Override
  public void generate(DdlWrite writer, DropIndex dropIndex) {
    if (platformInclude(dropIndex.getPlatforms())) {
      writer.apply().appendStatement(platformDdl.dropIndex(dropIndex.getIndexName(), dropIndex.getTableName(), Boolean.TRUE.equals(dropIndex.isConcurrent())));
    }
  }

  @Override
  public void generate(DdlWrite writer, AddUniqueConstraint constraint) {
    if (platformInclude(constraint.getPlatforms())) {
      if (DdlHelp.isDropConstraint(constraint.getColumnNames())) {
        writer.apply().appendStatement(platformDdl.alterTableDropUniqueConstraint(constraint.getTableName(), constraint.getConstraintName()));

      } else {
        String[] cols = split(constraint.getColumnNames());
        String[] nullableColumns = split(constraint.getNullableColumns());
        writer.apply().appendStatement(platformDdl.alterTableAddUniqueConstraint(constraint.getTableName(), constraint.getConstraintName(), cols, nullableColumns));
      }
    }
  }

  @Override
  public void generate(DdlWrite writer, AlterForeignKey alterForeignKey) {
    if (DdlHelp.isDropForeignKey(alterForeignKey.getColumnNames())) {
      writer.apply().appendStatement(platformDdl.alterTableDropForeignKey(alterForeignKey.getTableName(), alterForeignKey.getName()));
    } else {
      writer.apply().appendStatement(platformDdl.alterTableAddForeignKey(writer.getOptions(), new WriteForeignKey(alterForeignKey)));
    }
  }

  /**
   * Add add history table DDL.
   */
  @Override
  public void generate(DdlWrite writer, AddHistoryTable addHistoryTable) {
    platformDdl.addHistoryTable(writer, addHistoryTable);
  }

  /**
   * Add drop history table DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    platformDdl.dropHistoryTable(writer, dropHistoryTable);
  }

  @Override
  public void generateProlog(DdlWrite writer) {
    platformDdl.generateProlog(writer);
  }

  /**
   * Called at the end to generate additional ddl such as regenerate history triggers.
   */
  @Override
  public void generateEpilog(DdlWrite writer) {
    if (!regenerateHistoryTriggers.isEmpty()) {
      platformDdl.lockTables(writer.applyHistoryTrigger(), regenerateHistoryTriggers.keySet());

      for (HistoryTableUpdate update : this.regenerateHistoryTriggers.values()) {
        platformDdl.regenerateHistoryTriggers(writer, update);
      }

      platformDdl.unlockTables(writer.applyHistoryTrigger(), regenerateHistoryTriggers.keySet());
    }
    platformDdl.generateEpilog(writer);
  }

  @Override
  public void generate(DdlWrite writer, AddTableComment addTableComment) {
    if (hasValue(addTableComment.getComment())) {
      platformDdl.addTableComment(writer.apply(), addTableComment.getName(), addTableComment.getComment());
    }
  }

  /**
   * Add add column DDL.
   */
  @Override
  public void generate(DdlWrite writer, AddColumn addColumn) {
    String tableName = addColumn.getTableName();
    List<Column> columns = addColumn.getColumn();
    for (Column column : columns) {
      alterTableAddColumn(writer, tableName, column, false, isTrue(addColumn.isWithHistory()));
    }
    if (isTrue(addColumn.isWithHistory()) && historySupport == HistorySupport.TRIGGER_BASED) {
      // make same changes to the history table
      String historyTable = historyTable(tableName);
      for (Column column : columns) {
        regenerateHistoryTriggers(tableName, HistoryTableUpdate.Change.ADD, column.getName());
        alterTableAddColumn(writer, historyTable, column, true, true);
      }
    }
    for (Column column : columns) {
      if (hasValue(column.getReferences())) {
        writeForeignKey(writer, tableName, column);
      }
    }
    writer.apply().end();
  }

  /**
   * Add drop table DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropTable dropTable) {
    dropTable(writer.apply(), dropTable.getName());
    if (hasValue(dropTable.getSequenceCol())
        && platformDdl.getPlatform().getDbIdentity().isSupportsSequence()) {
      String sequenceName = dropTable.getSequenceName();
      if (!hasValue(sequenceName)) {
        sequenceName = namingConvention.getSequenceName(dropTable.getName(), dropTable.getSequenceCol());
      }
      dropSequence(writer.apply(), sequenceName);
    }
  }

  /**
   * Add drop column DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropColumn dropColumn) {
    String tableName = dropColumn.getTableName();
    alterTableDropColumn(writer, tableName, dropColumn.getColumnName());

    if (isTrue(dropColumn.isWithHistory())  && historySupport == HistorySupport.TRIGGER_BASED) {
      // also drop from the history table
      regenerateHistoryTriggers(tableName, HistoryTableUpdate.Change.DROP, dropColumn.getColumnName());
      alterTableDropColumn(writer, historyTable(tableName), dropColumn.getColumnName());
    }
    writer.apply().end();
  }

  /**
   * Add all the appropriate changes based on the column changes.
   */
  @Override
  public void generate(DdlWrite writer, AlterColumn alterColumn) {
    DdlMigrationHelp ddlHelp = new DdlMigrationHelp(alterColumn);
    ddlHelp.writeBefore(writer.apply());

    if (isTrue(alterColumn.isHistoryExclude())) {
      regenerateHistoryTriggers(alterColumn.getTableName(), HistoryTableUpdate.Change.EXCLUDE, alterColumn.getColumnName());
    } else if (isFalse(alterColumn.isHistoryExclude())) {
      regenerateHistoryTriggers(alterColumn.getTableName(), HistoryTableUpdate.Change.INCLUDE, alterColumn.getColumnName());
    }

    if (hasValue(alterColumn.getDropForeignKey())) {
      alterColumnDropForeignKey(writer, alterColumn);
    }
    if (hasValue(alterColumn.getReferences())) {
      alterColumnAddForeignKey(writer, alterColumn);
    }

    if (hasValue(alterColumn.getDropUnique())) {
      alterColumnDropUniqueConstraint(writer, alterColumn);
    }
    if (hasValue(alterColumn.getUnique())) {
      alterColumnAddUniqueConstraint(writer, alterColumn);
    }
    if (hasValue(alterColumn.getUniqueOneToOne())) {
      alterColumnAddUniqueOneToOneConstraint(writer, alterColumn);
    }
    if (hasValue(alterColumn.getComment())) {
      alterColumnComment(writer, alterColumn);
    }
    if (hasValue(alterColumn.getDropCheckConstraint())) {
      dropCheckConstraint(writer, alterColumn, alterColumn.getDropCheckConstraint());
    }

    boolean alterCheckConstraint = hasValue(alterColumn.getCheckConstraint());

    if (alterCheckConstraint) {
      // drop constraint before altering type etc
      dropCheckConstraint(writer, alterColumn, alterColumn.getCheckConstraintName());
    }

    if (hasValue(alterColumn.getType())
      || hasValue(alterColumn.getDefaultValue())
      || alterColumn.isNotnull() != null) {
      alterColumn(writer, alterColumn);
    }

    if (alterCheckConstraint) {
      // add constraint last (after potential type change)
      addCheckConstraint(writer, alterColumn);
    }
    ddlHelp.writeAfter(writer.apply());
  }

  private void alterColumnComment(DdlWrite writer, AlterColumn alterColumn) {
    platformDdl.addColumnComment(writer.apply(), alterColumn.getTableName(), alterColumn.getColumnName(), alterColumn.getComment());
  }

  /**
   * Return the name of the history table given the base table name.
   */
  protected String historyTable(String baseTable) {
    return baseTable + historyTableSuffix;
  }

  /**
   * Register the base table that we need to regenerate the history triggers on.
   */
  protected void regenerateHistoryTriggers(String baseTableName, HistoryTableUpdate.Change change, String column) {
    HistoryTableUpdate update = regenerateHistoryTriggers.computeIfAbsent(baseTableName, HistoryTableUpdate::new);
    update.add(change, column);
  }

  /**
   * alter all the base attributes (type/default/notnull) of the column together.
   * Some platforms (like mysql/sqlserver/hana) must do that in one statement,
   * other platforms may use several statements for altering one of the base
   * attributes.
   */
  protected void alterColumn(DdlWrite writer, AlterColumn alter) {
    platformDdl.alterColumn(writer, alter);

    if (isTrue(alter.isWithHistory()) && historySupport == HistorySupport.TRIGGER_BASED) {
      // we will apply only type changes or notNull -> null transition
      boolean isNull = Boolean.FALSE.equals(alter.isNotnull());
      boolean applyToHistory = alter.getType() != null || isNull;
      if (applyToHistory) {
        regenerateHistoryTriggers(alter.getTableName(), HistoryTableUpdate.Change.ALTER, alter.getColumnName());
        AlterColumn alterHistoryColumn = new AlterColumn();
        alterHistoryColumn.setTableName(historyTable(alter.getTableName()));
        alterHistoryColumn.setColumnName(alter.getColumnName());
        // ignore default value (not needed on history tables)
        alterHistoryColumn.setCurrentType(alter.getCurrentType());
        alterHistoryColumn.setType(alter.getType());
        if (isNull) {
          // do transition from notNull to null
          alterHistoryColumn.setCurrentNotnull(Boolean.TRUE);
          alterHistoryColumn.setNotnull(Boolean.FALSE);
        } else {
          // assume that the column contains null values from the past (required for platforms like mysql/sqlserver/hana)
          alterHistoryColumn.setCurrentNotnull(Boolean.FALSE);
        }
        platformDdl.alterColumn(writer, alterHistoryColumn);
      }

    }
  }

  protected void dropCheckConstraint(DdlWrite writer, AlterColumn alter, String constraintName) {
    writer.apply().appendStatement(platformDdl.alterTableDropConstraint(alter.getTableName(), constraintName));
  }

  protected void addCheckConstraint(DdlWrite writer, AlterColumn alter) {
    writer.apply().appendStatement(platformDdl.alterTableAddCheckConstraint(alter.getTableName(), alter.getCheckConstraintName(), alter.getCheckConstraint()));
  }


  protected void alterColumnAddForeignKey(DdlWrite writer, AlterColumn alterColumn) {
    alterTableAddForeignKey(writer.getOptions(), writer.apply(), new WriteForeignKey(alterColumn));
  }

  protected void alterColumnDropForeignKey(DdlWrite writer, AlterColumn alter) {
    writer.apply().appendStatement(platformDdl.alterTableDropForeignKey(alter.getTableName(), alter.getDropForeignKey()));
  }

  protected void alterColumnDropUniqueConstraint(DdlWrite writer, AlterColumn alter) {
    writer.apply().appendStatement(platformDdl.alterTableDropUniqueConstraint(alter.getTableName(), alter.getDropUnique()));
  }

  protected void alterColumnAddUniqueOneToOneConstraint(DdlWrite writer, AlterColumn alter) {
    addUniqueConstraint(writer, alter, alter.getUniqueOneToOne());
  }

  protected void alterColumnAddUniqueConstraint(DdlWrite writer, AlterColumn alter) {
    addUniqueConstraint(writer, alter, alter.getUnique());
  }

  protected void addUniqueConstraint(DdlWrite writer, AlterColumn alter, String uqName) {
    String[] cols = {alter.getColumnName()};
    boolean notNull = alter.isNotnull() != null ? alter.isNotnull() : Boolean.TRUE.equals(alter.isNotnull());
    writer.apply().appendStatement(platformDdl.alterTableAddUniqueConstraint(alter.getTableName(), uqName, cols, notNull ? null : cols));

    writer.dropAllForeignKeys().appendStatement(platformDdl.dropIndex(uqName, alter.getTableName()));
  }


  protected void alterTableDropColumn(DdlWrite writer, String tableName, String columnName) {
    platformDdl.alterTableDropColumn(writer.apply(), tableName, columnName);
  }

  protected void alterTableAddColumn(DdlWrite writer, String tableName, Column column, boolean onHistoryTable, boolean withHistory) {
    DdlMigrationHelp help = new DdlMigrationHelp(tableName, column, withHistory);
    if (!onHistoryTable) {
      help.writeBefore(writer.apply());
    }

    platformDdl.alterTableAddColumn(writer.apply(), tableName, column, onHistoryTable, help.getDefaultValue());
    final String comment = column.getComment();
    if (comment != null && !comment.isEmpty()) {
      platformDdl.addColumnComment(writer.apply(), tableName, column.getName(), comment);
    }

    if (!onHistoryTable) {
      help.writeAfter(writer.apply());
    }
  }

  protected boolean isFalse(Boolean value) {
    return value != null && !value;
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

}
