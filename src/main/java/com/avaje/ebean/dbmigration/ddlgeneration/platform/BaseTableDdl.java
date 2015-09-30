package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.TableDdl;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.IndexSet;
import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.CreateIndex;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropIndex;
import com.avaje.ebean.dbmigration.migration.DropTable;
import com.avaje.ebean.dbmigration.migration.ForeignKey;
import com.avaje.ebean.dbmigration.migration.UniqueConstraint;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation for 'create table' and 'alter table' statements.
 */
public class BaseTableDdl implements TableDdl {

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
  protected List<Column> externalUnique = new ArrayList<Column>();

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
  protected Map<String, HistoryTableUpdate> regenerateHistoryTriggers = new LinkedHashMap<String, HistoryTableUpdate>();

  /**
   * Construct with a naming convention and platform specific DDL.
   */
  public BaseTableDdl(ServerConfig serverConfig, PlatformDdl platformDdl) {
    this.namingConvention = serverConfig.getNamingConvention();
    this.naming = serverConfig.getConstraintNaming();
    this.historyTableSuffix = serverConfig.getHistoryTableSuffix();
    this.platformDdl = platformDdl;
    this.platformDdl.configure(serverConfig);
  }

  /**
   * Reset counters and index set for each table processed.
   */
  protected void reset() {
    indexSet.clear();
    externalUnique.clear();
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

    DdlBuffer apply = writer.apply();
    apply.append("create table ").append(tableName).append(" (");
    writeTableColumns(apply, columns, useIdentity);
    writeCheckConstraints(apply, createTable);
    writeUniqueConstraints(apply, createTable);
    writeCompoundUniqueConstraints(apply, createTable);
    if (!pk.isEmpty()) {
      // defined on the columns
      writePrimaryKeyConstraint(apply, createTable.getPkName(), toColumnNames(pk));
    }

    apply.newLine().append(")").endOfStatement();

    writeUniqueOneToOneConstraints(writer, createTable);

    if (isTrue(createTable.isWithHistory())) {
      // create history with rollback before the
      // associated drop table is written to rollback
      createWithHistory(writer, createTable.getName());
    }

    // add drop table to the rollback buffer - do this before
    // we drop the related sequence (if sequences are used)
    dropTable(writer.rollback(), tableName);

    if (useSequence) {
      String pkCol = pk.get(0).getName();
      writeSequence(writer, createTable, pkCol);
    }

    // add blank line for a bit of whitespace between tables
    apply.end();
    writer.rollback().end();

    writeAddForeignKeys(writer, createTable);

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
      String[] columnNames = {col.getName()};
      write.apply()
          .append(platformDdl.alterTableAddUniqueConstraint(tableName, uqName, columnNames))
          .endOfStatement();

      write.rollbackForeignKeys()
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
      writer.rollback().append(platformDdl.dropSequence(seqName)).endOfStatement();
    }
  }

  protected void createWithHistory(DdlWrite writer, String name) throws IOException {

    MTable table = writer.getTable(name);
    platformDdl.createWithHistory(writer, table);
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

      String refTableName = key.getRefTableName();
      String fkName = key.getName();
      String[] cols = toColumnNamesSplit(key.getColumnNames());
      String[] refColumns = toColumnNamesSplit(key.getRefColumnNames());

      writeForeignKey(write, fkName, tableName, cols, refTableName, refColumns, key.getIndexName());
    }
  }

  protected void writeForeignKey(DdlWrite write, String tableName, Column column) throws IOException {

    String fkName = column.getForeignKeyName();
    String references = column.getReferences();
    int pos = references.lastIndexOf('.');
    if (pos == -1) {
      throw new IllegalStateException("Expecting period '.' character for table.column split but not found in [" + references + "]");
    }
    String refTableName = references.substring(0, pos);
    String refColumnName = references.substring(pos + 1);

    String[] cols = {column.getName()};
    String[] refCols = {refColumnName};

    writeForeignKey(write, fkName, tableName, cols, refTableName, refCols, column.getForeignKeyIndex());
  }

  protected void writeForeignKey(DdlWrite write, String fkName, String tableName, String[] columns, String refTable, String[] refColumns, String indexName) throws IOException {

    tableName = lowerTableName(tableName);
    DdlBuffer fkeyBuffer = write.applyForeignKeys();
    alterTableAddForeignKey(fkeyBuffer, fkName, tableName, columns, refTable, refColumns);

    if (indexName != null) {
      // no matching unique constraint so add the index
      fkeyBuffer.append(platformDdl.createIndex(indexName, tableName, columns)).endOfStatement();
    }

    fkeyBuffer.end();

    write.rollbackForeignKeys()
        .append(platformDdl.alterTableDropForeignKey(tableName, fkName)).endOfStatement();

    if (indexName != null) {
      write.rollbackForeignKeys()
          .append(platformDdl.dropIndex(indexName, tableName)).endOfStatement();
    }

    write.rollbackForeignKeys().end();

  }

  protected void alterTableAddForeignKey(DdlBuffer buffer, String fkName, String tableName, String[] columns, String refTable, String[] refColumns) throws IOException {

    buffer.append(platformDdl.alterTableAddForeignKey(tableName, fkName, columns, refTable, refColumns)).endOfStatement();
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
    for (UniqueConstraint uniqueConstraint : uniqueConstraints) {
      String uqName = uniqueConstraint.getName();
      String[] columns = toColumnNamesSplit(uniqueConstraint.getColumnNames());
      apply.append(",").newLine();
      apply.append("  constraint ").append(uqName).append(" unique");
      appendColumns(columns, apply);
    }
  }

  /**
   * Write the unique constraints inline with the create table statement.
   */
  protected void writeUniqueConstraints(DdlBuffer apply, CreateTable createTable) throws IOException {

    boolean inlineUniqueOneToOne = platformDdl.isInlineUniqueOneToOne();

    List<Column> columns = createTable.getColumn();
    for (Column column : columns) {
      if (hasValue(column.getUnique()) || (inlineUniqueOneToOne && hasValue(column.getUniqueOneToOne()))) {
        // normal mechanism for adding unique constraint
        inlineUniqueConstraintSingle(apply, column);

      } else if (!inlineUniqueOneToOne && hasValue(column.getUniqueOneToOne())) {
        // MsSqlServer specific mechanism for adding unique constraints (that allow nulls)
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
   * Return as an array of string column names.
   */
  protected String[] toColumnNamesSplit(String columns) {
    return columns.split(",");
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
    List<Column> pk = new ArrayList<Column>(3);
    for (Column column : columns) {
      if (isTrue(column.isPrimaryKey())) {
        pk.add(column);
      }
    }
    return pk;
  }

  @Override
  public void generate(DdlWrite writer, CreateIndex createIndex) throws IOException {

    String[] cols = toColumnNamesSplit(createIndex.getColumns());
    writer.apply()
        .append(platformDdl.createIndex(createIndex.getIndexName(), createIndex.getTableName(), cols))
        .endOfStatement();

    writer.rollback()
        .append(platformDdl.dropIndex(createIndex.getIndexName(), createIndex.getTableName()))
        .endOfStatement();
  }

  @Override
  public void generate(DdlWrite writer, DropIndex dropIndex) throws IOException {

    writer.apply()
        .append(platformDdl.dropIndex(dropIndex.getIndexName(), dropIndex.getTableName()))
        .endOfStatement();
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

  /**
   * Called at the end to generate additional ddl such as regenerate history triggers.
   */
  @Override
  public void generateExtra(DdlWrite write) throws IOException {
    for (HistoryTableUpdate update : this.regenerateHistoryTriggers.values()) {
      platformDdl.regenerateHistoryTriggers(write, update);
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
      alterTableAddColumn(writer.apply(), tableName, column, false);
      alterTableDropColumn(writer.rollback(), tableName, column.getName());
    }

    if (isTrue(addColumn.isWithHistory())) {
      // make same changes to the history table
      String historyTable = historyTable(tableName);
      for (Column column : columns) {
        regenerateHistoryTriggers(tableName, HistoryTableUpdate.Change.ADD, column.getName());
        alterTableAddColumn(writer.apply(), historyTable, column, true);
        alterTableDropColumn(writer.rollback(), historyTable, column.getName());
      }
    }

    // add a bit of whitespace
    writer.apply().end();
    writer.rollback().end();
  }

  /**
   * Add drop table DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropTable dropTable) throws IOException {

    dropTable(writer.drop(), dropTable.getName());
  }

  /**
   * Add drop column DDL.
   */
  @Override
  public void generate(DdlWrite writer, DropColumn dropColumn) throws IOException {

    String tableName = dropColumn.getTableName();

    alterTableDropColumn(writer.drop(), tableName, dropColumn.getColumnName());
    if (isTrue(dropColumn.isWithHistory())) {
      // also drop from the history table
      regenerateHistoryTriggers(tableName, HistoryTableUpdate.Change.DROP, dropColumn.getColumnName());
      alterTableDropColumn(writer.drop(), historyTable(tableName), dropColumn.getColumnName());
    }

    writer.drop().end();
  }

  /**
   * Add all the appropriate changes based on the column changes.
   */
  @Override
  public void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException {

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

    HistoryTableUpdate update = regenerateHistoryTriggers.get(baseTableName);
    if (update == null) {
      update = new HistoryTableUpdate(baseTableName);
      regenerateHistoryTriggers.put(baseTableName, update);
    }
    update.add(change, column);
  }

  /**
   * This is mysql specific - alter all the base attributes of the column together.
   */
  protected void alterColumnBaseAttributes(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnBaseAttributes(alter);
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();

      // reverse and generate the rollback statement
      String currentType = alter.getCurrentType();
      String type = alter.getType();
      Boolean currentNotnull = alter.isCurrentNotnull();
      Boolean notnull = alter.isNotnull();

      alter.setCurrentType(type);
      alter.setType(currentType);
      alter.setNotnull(currentNotnull);
      alter.setCurrentNotnull(notnull);

      // write the rollback
      ddl = platformDdl.alterColumnBaseAttributes(alter);
      writer.rollback().append(ddl).endOfStatement();

      if (isTrue(alter.isWithHistory()) && alter.getType() != null) {
        // mysql and sql server column type change allowing nulls in the history table column
        AlterColumn alterHistoryColumn = new AlterColumn();
        alterHistoryColumn.setTableName(historyTable(alter.getTableName()));
        alterHistoryColumn.setColumnName(alter.getColumnName());
        alterHistoryColumn.setType(alter.getType());
        String histColumnDdl = platformDdl.alterColumnBaseAttributes(alterHistoryColumn);

        // write the apply to history table
        writer.apply().append(histColumnDdl).endOfStatement();

        // write the rollback from history table
        alterHistoryColumn.setType(currentType);
        histColumnDdl = platformDdl.alterColumnBaseAttributes(alterHistoryColumn);
        writer.rollback().append(histColumnDdl).endOfStatement();
      }
    }
  }

  protected void alterColumnDefaultValue(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnDefaultValue(alter.getTableName(), alter.getColumnName(), alter.getDefaultValue());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
    }
  }

  protected void alterColumnNotnull(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnNotnull(alter.getTableName(), alter.getColumnName(), alter.isNotnull());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
      ddl = platformDdl.alterColumnNotnull(alter.getTableName(), alter.getColumnName(), alter.isCurrentNotnull());
      writer.rollback().append(ddl).endOfStatement();
    }
  }

  protected void alterColumnType(DdlWrite writer, AlterColumn alter) throws IOException {

    String ddl = platformDdl.alterColumnType(alter.getTableName(), alter.getColumnName(), alter.getType());
    if (hasValue(ddl)) {
      writer.apply().append(ddl).endOfStatement();
      ddl = platformDdl.alterColumnType(alter.getTableName(), alter.getColumnName(), alter.getCurrentType());
      writer.rollback().append(ddl).endOfStatement();
      if (isTrue(alter.isWithHistory())) {
        // apply same type change to matching column in the history table
        ddl = platformDdl.alterColumnType(historyTable(alter.getTableName()), alter.getColumnName(), alter.getType());
        writer.apply().append(ddl).endOfStatement();
        ddl = platformDdl.alterColumnType(historyTable(alter.getTableName()), alter.getColumnName(), alter.getCurrentType());
        writer.rollback().append(ddl).endOfStatement();
      }
    }
  }


  protected void alterColumnAddForeignKey(DdlWrite writer, AlterColumn alterColumn) throws IOException {

    String tableName = alterColumn.getTableName();
    String fkName = alterColumn.getForeignKeyName();
    String[] cols = {alterColumn.getColumnName()};
    String references = alterColumn.getReferences();
    int pos = references.lastIndexOf('.');
    if (pos == -1) {
      throw new IllegalStateException("Expecting period '.' character for table.column split but not found in [" + references + "]");
    }
    String refTableName = references.substring(0, pos);
    String refColumnName = references.substring(pos + 1);
    String[] refCols = {refColumnName};

    alterTableAddForeignKey(writer.apply(), fkName, tableName, cols, refTableName, refCols);
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

    writer.apply()
        .append(platformDdl.alterTableAddUniqueConstraint(alter.getTableName(), uqName, cols))
        .endOfStatement();

    writer.rollbackForeignKeys()
        .append(platformDdl.dropIndex(uqName, alter.getTableName()))
        .endOfStatement();
  }


  protected void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) throws IOException {

    buffer.append("alter table ").append(tableName).append(" drop column ").append(columnName)
        .endOfStatement();
  }

  protected void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column, boolean onHistoryTable) throws IOException {

    buffer.append("alter table ").append(tableName)
        .append(" add column ").append(column.getName())
        .append(" ").append(column.getType());

    if (!onHistoryTable) {
      if (isTrue(column.isNotnull())) {
        buffer.append(" not null");
      }
      if (hasValue(column.getCheckConstraint())) {
        buffer.append(" ").append(column.getCheckConstraint());
      }
    }
    buffer.endOfStatement();
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
