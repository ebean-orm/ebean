package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DbConstraintNaming;
import io.ebean.config.NamingConvention;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DbHistorySupport;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.TableDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.IndexSet;
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
   * Used to check that indexes on foreign keys should be skipped as a unique index on the columns
   * already exists.
   */
  protected IndexSet indexSet = new IndexSet();

  /**
   * Used when unique constraints specifically for OneToOne can't be created normally (MsSqlServer).
   */
  protected List<Column> externalUnique = new ArrayList<>();

  protected List<UniqueConstraint> externalCompoundUnique = new ArrayList<>();

  // counters used when constraint names are truncated due to maximum length
  // and these counters are used to keep the constraint name unique
  protected int countCheck;
  protected int countUnique;
  protected int countForeignKey;
  protected int countIndex;

  /**
   * Base tables that have associated history tables that need their triggers/functions regenerated as
   * columns have been added, removed, included or excluded.
   */
  protected Map<String, HistoryTableUpdate> regenerateHistoryTriggers = new LinkedHashMap<>();

  private boolean strictMode;

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

      before = getScriptsForPlatform(column.getBefore(), platformDdl.getPlatform().getName());
      after = getScriptsForPlatform(column.getAfter(), platformDdl.getPlatform().getName());
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
        before = Arrays.asList(platformDdl.getUpdateNullWithDefault());
      } else {
        before = getScriptsForPlatform(alter.getBefore(), platformDdl.getPlatform().getName());
      }
      after = getScriptsForPlatform(alter.getAfter(), platformDdl.getPlatform().getName());
    }

    void writeBefore(DdlBuffer buffer) throws IOException {
      if (!before.isEmpty()) {
        buffer.end();
      }

      if (!before.isEmpty() && withHistory) {
        buffer.append("-- NOTE: table has @History - special migration may be necessary").newLine();
      }
      for (String ddlScript : before) {
        buffer.append(translate(ddlScript, tableName, columnName, this.defaultValue));
        buffer.endOfStatement();
      }
    }

    void writeAfter(DdlBuffer buffer) throws IOException {
      if (!after.isEmpty() && withHistory) {
        buffer.append("-- NOTE: table has @History - special migration may be necessary").newLine();
      }
      // here we run post migration scripts
      for (String ddlScript : after) {
        buffer.append(translate(ddlScript, tableName, columnName, defaultValue));
        buffer.endOfStatement();
      }
      if (!after.isEmpty()) {
        buffer.end();
      }
    }

    private List<String> getScriptsForPlatform(List<DdlScript> scripts, String searchPlatform) {
      List<String> ret = Collections.emptyList();
      for (DdlScript script : scripts) {
        if (script.getPlatforms() == null || script.getPlatforms().isEmpty()) {
          ret = script.getDdl();
        } else for (String platform : StringHelper.splitNames(script.getPlatforms())) {
          if (platform.equals(searchPlatform)) {
           return script.getDdl();
          }
        }
      }
      return ret;
    }

    /**
     * Replaces Table name (${table}), Column name (${column}) and default value (${default}) in DDL.
     */
    private String translate(String ddl, String tableName, String columnName, String defaultValue) {
      String ret = StringHelper.replaceString(ddl, "${table}", tableName);
      ret = StringHelper.replaceString(ret, "${column}", columnName);
      return StringHelper.replaceString(ret, "${default}", defaultValue);
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
  public BaseTableDdl(ServerConfig serverConfig, PlatformDdl platformDdl) {
    this.namingConvention = serverConfig.getNamingConvention();
    this.naming = serverConfig.getConstraintNaming();
    this.historyTableSuffix = serverConfig.getHistoryTableSuffix();
    this.platformDdl = platformDdl;
    this.platformDdl.configure(serverConfig);
    this.strictMode = serverConfig.getMigrationConfig().isStrictMode();
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
    indexSet.clear();
    externalUnique.clear();
    externalCompoundUnique.clear();
    countCheck = 0;
    countUnique = 0;
    countForeignKey = 0;
    countIndex = 0;
  }

  /**
   * Generate the appropriate 'create table' and matching 'drop table' statements
   * and add them to the appropriate 'apply' and 'rollback' buffers.
   */
  @Override
  public void generate(DdlWrite writer, CreateTable createTable) throws IOException {

    reset();

    String tableName = lowerTableName(createTable.getName());
    List<Column> columns = createTable.getColumn();
    List<Column> pk = determinePrimaryKeyColumns(columns);

    boolean singleColumnPrimaryKey = (pk.size() == 1);
    boolean useIdentity = false;
    boolean useSequence = false;

    if (singleColumnPrimaryKey) {
      IdType useDbIdentityType = platformDdl.useIdentityType(createTable.getIdentityType());
      useIdentity = (IdType.IDENTITY == useDbIdentityType);
      useSequence = (IdType.SEQUENCE == useDbIdentityType);
    }

    String partitionMode = createTable.getPartitionMode();

    DdlBuffer apply = writer.apply();
    apply.append("create table ").append(tableName).append(" (");
    writeTableColumns(apply, columns, useIdentity);
    writeCheckConstraints(apply, createTable);
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

    if (useSequence) {
      String pkCol = pk.get(0).getName();
      writeSequence(writer, createTable, pkCol);
    }

    // add blank line for a bit of whitespace between tables
    apply.end();
    writer.dropAll().end();

    if (!platformDdl.isInlineForeignKeys()) {
      writeAddForeignKeys(writer, createTable);
    }
  }

  /**
   * Add table and column comments (separate from the create table statement).
   */
  private void addComments(DdlBuffer apply, CreateTable createTable) throws IOException {
    if (!platformDdl.isInlineComments()) {
      String tableComment = createTable.getComment();
      if (!StringHelper.isNull(tableComment)) {
        platformDdl.addTableComment(apply, createTable.getName(), tableComment);
      }

      List<Column> columns = createTable.getColumn();
      for (Column column : columns) {
        if (!StringHelper.isNull(column.getComment())) {
          platformDdl.addColumnComment(apply, createTable.getName(), column.getName(), column.getComment());
        }
      }
    }
  }

  /**
   * Add the table comment inline with the create table statement.
   */
  private void addTableCommentInline(DdlBuffer apply, CreateTable createTable) throws IOException {
    if (platformDdl.isInlineComments()) {
      String tableComment = createTable.getComment();
      if (!StringHelper.isNull(tableComment)) {
        platformDdl.inlineTableComment(apply, tableComment);
      }
    }
  }

  private void writeTableColumns(DdlBuffer apply, List<Column> columns, boolean useIdentity) throws IOException {
    platformDdl.writeTableColumns(apply, columns, useIdentity);
  }

  /**
   * Specific handling of OneToOne unique constraints for MsSqlServer.
   * For all other DB platforms these unique constraints are done inline as per normal.
   */
  protected void writeUniqueOneToOneConstraints(DdlWrite write, CreateTable createTable) throws IOException {

    String tableName = createTable.getName();
    for (Column col : externalUnique) {
      String uqName = col.getUniqueOneToOne();
      if (uqName == null) {
        uqName = col.getUnique();
      }
      String[] columnNames = {col.getName()};
      write.apply()
        .append(platformDdl.alterTableAddUniqueConstraint(tableName, uqName, columnNames, Boolean.TRUE.equals(col.isNotnull()) ? null : columnNames))
        .endOfStatement();

      write.dropAllForeignKeys()
        .append(platformDdl.dropIndex(uqName, tableName))
        .endOfStatement();
    }

    for (UniqueConstraint constraint : externalCompoundUnique) {
      String uqName = constraint.getName();
      String[] columnNames = SplitColumns.split(constraint.getColumnNames());
      String[] nullableColumns = SplitColumns.split(constraint.getNullableColumns());
      write.apply()
        .append(platformDdl.alterTableAddUniqueConstraint(tableName, uqName, columnNames, nullableColumns))
        .endOfStatement();

      write.dropAllForeignKeys()
        .append(platformDdl.dropIndex(uqName, tableName))
        .endOfStatement();
    }
  }

  protected void writeSequence(DdlWrite writer, CreateTable createTable, String pk) throws IOException {

    // explicit sequence use or platform decides
    String explicitSequenceName = createTable.getSequenceName();
    int initial = toInt(createTable.getSequenceInitial());
    int allocate = toInt(createTable.getSequenceAllocate());

    String seqName = explicitSequenceName;
    if (seqName == null) {
      seqName = namingConvention.getSequenceName(createTable.getName(), pk);
    }

    String createSeq = platformDdl.createSequence(seqName, initial, allocate);
    if (createSeq != null) {
      writer.apply().append(createSeq).newLine();
      writer.dropAll().append(platformDdl.dropSequence(seqName)).endOfStatement();
    }
  }

  protected void createWithHistory(DdlWrite writer, String name) throws IOException {

    MTable table = writer.getTable(name);
    platformDdl.createWithHistory(writer, table);
  }

  protected void writeInlineForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    for (Column column : createTable.getColumn()) {
      String references = column.getReferences();
      if (hasValue(references)) {
        writeInlineForeignKey(write, column);
      }
    }
    writeInlineCompoundForeignKeys(write, createTable);
  }

  protected void writeInlineForeignKey(DdlWrite write, Column column) throws IOException {

    String fkConstraint = platformDdl.tableInlineForeignKey(new WriteForeignKey(null, column));
    write.apply().append(",").newLine().append("  ").append(fkConstraint);
  }

  protected void writeInlineCompoundForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    List<ForeignKey> foreignKey = createTable.getForeignKey();
    for (ForeignKey key : foreignKey) {
      String fkConstraint = platformDdl.tableInlineForeignKey(new WriteForeignKey(null, key));
      write.apply().append(",").newLine().append("  ").append(fkConstraint);
    }
  }

  protected void writeAddForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    String tableName = createTable.getName();
    List<Column> columns = createTable.getColumn();
    for (Column column : columns) {
      String references = column.getReferences();
      if (hasValue(references)) {
        writeForeignKey(write, tableName, column);
      }
    }

    writeAddCompoundForeignKeys(write, createTable);
  }

  protected void writeAddCompoundForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    String tableName = createTable.getName();

    List<ForeignKey> foreignKey = createTable.getForeignKey();
    for (ForeignKey key : foreignKey) {
      writeForeignKey(write, new WriteForeignKey(tableName, key));
    }
  }

  protected void writeForeignKey(DdlWrite write, String tableName, Column column) throws IOException {
    writeForeignKey(write, new WriteForeignKey(tableName, column));
  }

  protected void writeForeignKey(DdlWrite write, WriteForeignKey request) throws IOException {

    DdlBuffer fkeyBuffer = write.applyForeignKeys();
    String tableName = lowerTableName(request.table());
    if (request.indexName() != null) {
      // no matching unique constraint so add the index
      fkeyBuffer.append(platformDdl.createIndex(request.indexName(), tableName, request.cols())).endOfStatement();
    }

    alterTableAddForeignKey(fkeyBuffer, request);

    fkeyBuffer.end();

    write.dropAllForeignKeys()
      .append(platformDdl.alterTableDropForeignKey(tableName, request.fkName())).endOfStatement();

    if (request.indexName() != null) {
      write.dropAllForeignKeys()
        .append(platformDdl.dropIndex(request.indexName(), tableName)).endOfStatement();
    }

    write.dropAllForeignKeys().end();
  }

  protected void alterTableAddForeignKey(DdlBuffer buffer, WriteForeignKey request) throws IOException {

    String fkConstraint = platformDdl.alterTableAddForeignKey(request);
    if (fkConstraint != null && !fkConstraint.isEmpty()) {
      buffer.append(fkConstraint).endOfStatement();
    }
  }

  protected void appendColumns(String[] columns, DdlBuffer buffer) throws IOException {
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
  protected void dropTable(DdlBuffer buffer, String tableName) throws IOException {

    buffer.append(platformDdl.dropTable(tableName)).endOfStatement();
  }

  /**
   * Add 'drop sequence' statement to the buffer.
   */
  protected void dropSequence(DdlBuffer buffer, String sequenceName) throws IOException {

    buffer.append(platformDdl.dropSequence(sequenceName)).endOfStatement();
  }

  /**
   * Write all the check constraints.
   */
  protected void writeCheckConstraints(DdlBuffer apply, CreateTable createTable) throws IOException {

    List<Column> columns = createTable.getColumn();
    for (Column column : columns) {
      String checkConstraint = column.getCheckConstraint();
      if (hasValue(checkConstraint)) {
        writeCheckConstraint(apply, column, checkConstraint);
      }
    }
  }

  /**
   * Write a check constraint.
   */
  protected void writeCheckConstraint(DdlBuffer buffer, Column column, String checkConstraint) throws IOException {

    String ckName = column.getCheckConstraintName();

    buffer.append(",").newLine();
    buffer.append("  constraint ").append(ckName);
    buffer.append(" ").append(checkConstraint);
  }

  protected void writeCompoundUniqueConstraints(DdlBuffer apply, CreateTable createTable) throws IOException {

    List<UniqueConstraint> uniqueConstraints = createTable.getUniqueConstraint();
    boolean inlineUniqueWhenNull = platformDdl.isInlineUniqueWhenNullable();
    for (UniqueConstraint uniqueConstraint : uniqueConstraints) {
       if (inlineUniqueWhenNull) {
        String uqName = uniqueConstraint.getName();
        String[] columns = SplitColumns.split(uniqueConstraint.getColumnNames());
        apply.append(",").newLine();
        apply.append("  constraint ").append(uqName).append(" unique");
        appendColumns(columns, apply);
      } else {
        externalCompoundUnique.add(uniqueConstraint);
      }
    }
  }

  /**
   * Write the unique constraints inline with the create table statement.
   */
  protected void writeUniqueConstraints(DdlBuffer apply, CreateTable createTable) throws IOException {

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
  protected void inlineUniqueConstraintSingle(DdlBuffer buffer, Column column) throws IOException {

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
  protected void writePrimaryKeyConstraint(DdlBuffer buffer, String pkName, String[] pkColumns) throws IOException {

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
  public void generate(DdlWrite writer, CreateIndex createIndex) throws IOException {

    String[] cols = SplitColumns.split(createIndex.getColumns());
    writer.apply()
      .append(platformDdl.createIndex(createIndex.getIndexName(), createIndex.getTableName(), cols))
      .endOfStatement();

    writer.dropAll()
      .append(platformDdl.dropIndex(createIndex.getIndexName(), createIndex.getTableName()))
      .endOfStatement();
  }

  @Override
  public void generate(DdlWrite writer, DropIndex dropIndex) throws IOException {

    writer.apply()
      .append(platformDdl.dropIndex(dropIndex.getIndexName(), dropIndex.getTableName()))
      .endOfStatement();
  }
  @Override
  public void generate(DdlWrite writer, AddUniqueConstraint constraint) throws IOException {

    if (DdlHelp.isDropConstraint(constraint.getColumnNames())) {
      String ddl = platformDdl.alterTableDropUniqueConstraint(constraint.getTableName(), constraint.getConstraintName());
      if (hasValue(ddl)) {
        writer.apply().append(ddl).endOfStatement();
      }
    } else {
      String[] cols = SplitColumns.split(constraint.getColumnNames());
      String[] nullableColumns = SplitColumns.split(constraint.getNullableColumns());
      String ddl = platformDdl.alterTableAddUniqueConstraint(constraint.getTableName(), constraint.getConstraintName(), cols, nullableColumns);
      if (hasValue(ddl)) {
        writer.apply().append(ddl).endOfStatement();
      }
    }
  }

  @Override
  public void generate(DdlWrite writer, AlterForeignKey alterForeignKey) throws IOException {
    if (DdlHelp.isDropForeignKey(alterForeignKey.getColumnNames())) {
      String ddl = platformDdl.alterTableDropForeignKey(alterForeignKey.getTableName(), alterForeignKey.getName());
      if (hasValue(ddl)) {
        writer.apply().append(ddl).endOfStatement();
      }
    } else {
      String ddl = platformDdl.alterTableAddForeignKey(new WriteForeignKey(alterForeignKey));
      if (hasValue(ddl)) {
        writer.apply().append(ddl).endOfStatement();
      }
    }
  }

  /**
   * Add add history table DDL.
   */
  @Override
  public void generate(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {
    platformDdl.addHistoryTable(writer, addHistoryTable);
  }

  /**
   * Add drop history table DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {
    platformDdl.dropHistoryTable(writer, dropHistoryTable);
  }

  @Override
  public void generateProlog(DdlWrite write) throws IOException {
    platformDdl.generateProlog(write);
  }

  /**
   * Called at the end to generate additional ddl such as regenerate history triggers.
   */
  @Override
  public void generateEpilog(DdlWrite write) throws IOException {
    if (!regenerateHistoryTriggers.isEmpty()) {
      platformDdl.lockTables(write.applyHistoryTrigger(), regenerateHistoryTriggers.keySet());

      for (HistoryTableUpdate update : this.regenerateHistoryTriggers.values()) {
        platformDdl.regenerateHistoryTriggers(write, update);
      }

      platformDdl.unlockTables(write.applyHistoryTrigger(), regenerateHistoryTriggers.keySet());
    }
    platformDdl.generateEpilog(write);
  }

  @Override
  public void generate(DdlWrite writer, AddTableComment addTableComment) throws IOException {
    if (hasValue(addTableComment.getComment())) {
      platformDdl.addTableComment(writer.apply(), addTableComment.getName(), addTableComment.getComment());
    }
  }

  /**
   * Add add column DDL.
   */
  @Override
  public void generate(DdlWrite writer, AddColumn addColumn) throws IOException {

    String tableName = addColumn.getTableName();
    List<Column> columns = addColumn.getColumn();
    for (Column column : columns) {
      alterTableAddColumn(writer.apply(), tableName, column, false, isTrue(addColumn.isWithHistory()));
    }

    if (isTrue(addColumn.isWithHistory()) && historySupport == HistorySupport.TRIGGER_BASED) {
      // make same changes to the history table
      String historyTable = historyTable(tableName);
      for (Column column : columns) {
        regenerateHistoryTriggers(tableName, HistoryTableUpdate.Change.ADD, column.getName());
        alterTableAddColumn(writer.apply(), historyTable, column, true, true);
      }
    }

    for (Column column : columns) {
      if (hasValue(column.getReferences())) {
        writeForeignKey(writer, tableName, column);
      }
    }

    // add a bit of whitespace
    writer.apply().end();
  }

  /**
   * Add drop table DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropTable dropTable) throws IOException {

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
  public void generate(DdlWrite writer, DropColumn dropColumn) throws IOException {

    String tableName = dropColumn.getTableName();

    alterTableDropColumn(writer.apply(), tableName, dropColumn.getColumnName());

    if (isTrue(dropColumn.isWithHistory())  && historySupport == HistorySupport.TRIGGER_BASED) {
      // also drop from the history table
      regenerateHistoryTriggers(tableName, HistoryTableUpdate.Change.DROP, dropColumn.getColumnName());
      alterTableDropColumn(writer.apply(), historyTable(tableName), dropColumn.getColumnName());
    }

    writer.apply().end();
  }

  /**
   * Add all the appropriate changes based on the column changes.
   */
  @Override
  public void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException {
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
    boolean alterBaseAttributes = false;
    if (hasValue(alterColumn.getType())) {
      alterColumnType(writer, alterColumn);
      alterBaseAttributes = true;
    }
    if (hasValue(alterColumn.getDefaultValue())) {
      alterColumnDefaultValue(writer, alterColumn);
      alterBaseAttributes = true;
    }
    if (alterColumn.isNotnull() != null) {
      alterColumnNotnull(writer, alterColumn);
      alterBaseAttributes = true;
    }
    if (alterBaseAttributes) {
      alterColumnBaseAttributes(writer, alterColumn);
    }
    if (alterCheckConstraint) {
      // add constraint last (after potential type change)
      addCheckConstraint(writer, alterColumn);
    }
    ddlHelp.writeAfter(writer.apply());
  }

  private void alterColumnComment(DdlWrite writer, AlterColumn alterColumn) throws IOException {
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
   * This is mysql specific - alter all the base attributes of the column together.
   */
  protected void alterColumnBaseAttributes(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnBaseAttributes(alter);
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();

      if (isTrue(alter.isWithHistory()) && alter.getType() != null && historySupport == historySupport.TRIGGER_BASED) {
        // mysql and sql server column type change allowing nulls in the history table column
        regenerateHistoryTriggers(alter.getTableName(), HistoryTableUpdate.Change.ALTER, alter.getColumnName());
        AlterColumn alterHistoryColumn = new AlterColumn();
        alterHistoryColumn.setTableName(historyTable(alter.getTableName()));
        alterHistoryColumn.setColumnName(alter.getColumnName());
        alterHistoryColumn.setType(alter.getType());
        String histColumnDdl = platformDdl.alterColumnBaseAttributes(alterHistoryColumn);

        // write the apply to history table
        writer.apply().append(histColumnDdl).endOfStatement();
      }
    }
  }

  protected void alterColumnDefaultValue(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnDefaultValue(alter.getTableName(), alter.getColumnName(), alter.getDefaultValue());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
    }
  }

  protected void dropCheckConstraint(DdlWrite writer, AlterColumn alter, String constraintName) throws IOException {

    String ddl = platformDdl.alterTableDropConstraint(alter.getTableName(), constraintName);
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
    }
  }

  protected void addCheckConstraint(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterTableAddCheckConstraint(alter.getTableName(), alter.getCheckConstraintName(), alter.getCheckConstraint());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
    }
  }

  protected void alterColumnNotnull(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnNotnull(alter.getTableName(), alter.getColumnName(), alter.isNotnull());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
    }
  }

  protected void alterColumnType(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnType(alter.getTableName(), alter.getColumnName(), alter.getType());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
      if (isTrue(alter.isWithHistory()) && historySupport == HistorySupport.TRIGGER_BASED) {
        regenerateHistoryTriggers(alter.getTableName(), HistoryTableUpdate.Change.ALTER, alter.getColumnName());
        // apply same type change to matching column in the history table
        ddl = platformDdl.alterColumnType(historyTable(alter.getTableName()), alter.getColumnName(), alter.getType());
        writer.apply().append(ddl).endOfStatement();
      }
    }
  }

  protected void alterColumnAddForeignKey(DdlWrite writer, AlterColumn alterColumn) throws IOException {

    alterTableAddForeignKey(writer.apply(), new WriteForeignKey(alterColumn));
  }

  protected void alterColumnDropForeignKey(DdlWrite writer, AlterColumn alter) throws IOException {

    writer.apply()
      .append(platformDdl.alterTableDropForeignKey(alter.getTableName(), alter.getDropForeignKey()))
      .endOfStatement();
  }


  protected void alterColumnDropUniqueConstraint(DdlWrite writer, AlterColumn alter) throws IOException {

    writer.apply()
      .append(platformDdl.alterTableDropUniqueConstraint(alter.getTableName(), alter.getDropUnique()))
      .endOfStatement();
  }

  protected void alterColumnAddUniqueOneToOneConstraint(DdlWrite writer, AlterColumn alter) throws IOException {

    addUniqueConstraint(writer, alter, alter.getUniqueOneToOne());
  }

  protected void alterColumnAddUniqueConstraint(DdlWrite writer, AlterColumn alter) throws IOException {

    addUniqueConstraint(writer, alter, alter.getUnique());
  }

  protected void addUniqueConstraint(DdlWrite writer, AlterColumn alter, String uqName) throws IOException {

    String[] cols = {alter.getColumnName()};
    boolean notNull = alter.isNotnull() != null ? alter.isNotnull() : Boolean.TRUE.equals(alter.isNotnull());
    writer.apply()
      .append(platformDdl.alterTableAddUniqueConstraint(alter.getTableName(), uqName, cols, notNull ? null : cols))
      .endOfStatement();

    writer.dropAllForeignKeys()
      .append(platformDdl.dropIndex(uqName, alter.getTableName()))
      .endOfStatement();
  }


  protected void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) throws IOException {
    platformDdl.alterTableDropColumn(buffer, tableName, columnName);
  }

  protected void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column, boolean onHistoryTable, boolean withHistory) throws IOException {
    DdlMigrationHelp help = new DdlMigrationHelp(tableName, column, withHistory);
    if (!onHistoryTable) {
      help.writeBefore(buffer);
    }

    platformDdl.alterTableAddColumn(buffer, tableName, column, onHistoryTable, help.getDefaultValue());

    if (!onHistoryTable) {
      help.writeAfter(buffer);
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

  /**
   * Return as an int value with 0 when it is null.
   */
  protected int toInt(BigInteger value) {
    return (value == null) ? 0 : value.intValue();
  }

}
