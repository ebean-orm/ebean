package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.TableDdl;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.ForeignKey;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation for 'create table' and 'alter table' statements.
 */
public class BaseTableDdl implements TableDdl {

  protected final DdlNamingConvention namingConvention;

  protected final PlatformDdl platformDdl;

  /**
   * Used to check that indexes on foreign keys should be skipped as a unique index on the columns
   * already exists.
   */
  protected IndexSet indexSet = new IndexSet();

  // counters used when constraint names are truncated due to maximum length
  // and these counters are used to keep the constraint name unique
  protected int countCheck;
  protected int countUnique;
  protected int countForeignKey;
  protected int countIndex;

  /**
   * Construct with a naming convention and platform specific DDL.
   */
  public BaseTableDdl(DdlNamingConvention namingConvention, PlatformDdl platformDdl) {
    this.namingConvention = namingConvention;
    this.platformDdl = platformDdl;
  }

  /**
   * Reset counters and index set for each table.
   */
  protected void reset() {
    indexSet.clear();
    countCheck = 0;
    countUnique = 0;
    countForeignKey = 0;
    countIndex = 0;
  }

  /**
   * Generate the appropriate 'create table' and matching 'drop table' statements
   * and add them to the 'apply' and 'rollback' buffers.
   */
  @Override
  public void generate(DdlWrite writer, CreateTable createTable) throws IOException {

    reset();

    String tableName = lowerName(createTable.getName());
    List<Column> columns = createTable.getColumn();
    List<Column> pk = determinePrimaryKeyColumns(columns);

    boolean singleColumnPrimaryKey = pk.size() == 1;
    boolean useIdentity = false;
    boolean useSequence = false;

    if (singleColumnPrimaryKey) {
      IdType useDbIdentityType = platformDdl.useIdentityType(createTable.getIdentityType());
      useIdentity = (IdType.IDENTITY == useDbIdentityType);
      useSequence = (IdType.SEQUENCE == useDbIdentityType);
    }

    DdlBuffer apply = writer.apply();
    apply.append("create table ").append(tableName).append(" (");
    for (int i = 0; i < columns.size(); i++) {
      apply.newLine();
      writeColumnDefinition(apply, columns.get(i), useIdentity);
      if (i < columns.size() - 1) {
        apply.append(",");
      }
    }

    writeCheckConstraints(apply, createTable);
    writeUniqueConstraints(apply, createTable);
    writeCompoundUniqueConstraints(apply, createTable);
    if (!pk.isEmpty()) {
      // defined on the columns
      writePrimaryKeyConstraint(apply, tableName, toColumnNames(pk));
    }

    apply.newLine().append(")").endOfStatement();

    // add drop table to the rollback buffer - do this before
    // we drop the related sequence (if sequences are used)
    dropTable(writer.rollback(), tableName);

    if (useSequence) {
      writeSequence(writer, createTable);
    }

    // add blank line for a bit of whitespace between tables
    apply.end();
    writer.rollback().end();

    writeAddForeignKeys(writer, createTable);

    if (isTrue(createTable.isWithHistory())) {
      createWithHistory(writer, createTable.getName());
    }

  }

  private void writeSequence(DdlWrite writer, CreateTable createTable) throws IOException {

    // explicit sequence use or platform decides
    String explicitSequenceName = createTable.getSequenceName();
    int initial = toInt(createTable.getSequenceInitial());
    int allocate = toInt(createTable.getSequenceAllocate());

    String seqName = namingConvention.sequenceName(createTable.getName(), explicitSequenceName);
    String createSeq = platformDdl.createSequence(seqName, initial, allocate);
    if (createSeq != null) {
      writer.apply().append(createSeq).newLine();
      writer.rollback().append(platformDdl.dropSequence(seqName)).endOfStatement();
    }
  }

  private void createWithHistory(DdlWrite writer, String name) throws IOException {

    MTable table = writer.getTable(name);
    platformDdl.createWithHistory(writer, table);
  }

  protected void writeAddForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    String tableName = createTable.getName();
    List<Column> columns = createTable.getColumn();
    for (Column column : columns) {
      String references = column.getReferences();
      if (hasValue(references)) {
        writeForeignKey(write, tableName, column.getName(), references);
      }
    }

    writeAddCompoundForeignKeys(write, createTable);
  }

  protected void writeAddCompoundForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    String tableName = createTable.getName();

    List<ForeignKey> foreignKey = createTable.getForeignKey();
    for (ForeignKey key : foreignKey) {

      String refTableName = key.getRefTableName();
      String fkName = determineForeignKeyConstraintName(tableName, refTableName);
      String[] cols = toColumnNamesSplit(key.getColumnNames());
      String[] refColumns = toColumnNamesSplit(key.getRefColumnNames());

      writeForeignKey(write, fkName, tableName, cols, refTableName, refColumns);
    }

  }

  protected void writeForeignKey(DdlWrite write, String tableName, String columnName, String references) throws IOException {

    String fkName = determineForeignKeyConstraintName(tableName, columnName);

    int pos = references.lastIndexOf('.');
    if (pos == -1) {
      throw new IllegalStateException("Expecting period '.' character for table.column split but not found in [" + references + "]");
    }
    String refTableName = references.substring(0, pos);
    String refColumnName = references.substring(pos + 1);

    String[] cols = {columnName};
    String[] refCols = {refColumnName};

    writeForeignKey(write, fkName, tableName, cols, refTableName, refCols);
  }

  protected void writeForeignKey(DdlWrite write, String fkName, String tableName, String[] columns, String refTable, String[] refColumns) throws IOException {

    tableName = lowerName(tableName);
    DdlBuffer fkeyBuffer = write.applyForeignKeys();
    fkeyBuffer
        .append("alter table ").append(tableName)
        .append(" add constraint ").append(fkName)
        .append(" foreign key");
    appendColumns(columns, fkeyBuffer);
    fkeyBuffer
        .append(" references ")
        .append(lowerName(refTable));
    appendColumns(refColumns, fkeyBuffer);
    fkeyBuffer.appendWithSpace(platformDdl.getForeignKeyRestrict())
        .endOfStatement();

    String indexName = determineForeignKeyIndexName(tableName, columns);

    boolean addIndex = indexSet.add(columns);
    if (addIndex) {
      // no matching unique constraint so add the index
      fkeyBuffer.append("create index ").append(indexName).append(" on ").append(tableName);
      appendColumns(columns, fkeyBuffer);
      fkeyBuffer.endOfStatement();
    }

    fkeyBuffer.end();

    if (addIndex) {
      write.rollbackForeignKeys()
          .append("drop index ").append(indexName)
          .endOfStatement();
    }

    write.rollbackForeignKeys()
        .append("alter table ").append(tableName).append(" drop constraint ").append(fkName)
        .endOfStatement();

    write.rollbackForeignKeys().end();

  }

  private void appendColumns(String[] columns, DdlBuffer buffer) throws IOException {
    buffer.append(" (");
    for (int i = 0; i <columns.length ; i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(lowerName(columns[i].trim()));
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
        writeCheckConstraint(apply, createTable.getName(), column, checkConstraint);
      }
    }
  }

  /**
   * Write a check constraint.
   */
  protected void writeCheckConstraint(DdlBuffer buffer, String tableName, Column column, String checkConstraint) throws IOException {

    String ckName = determineCheckConstraintName(tableName, column.getName());

    buffer.append(",").newLine();
    buffer.append("  constraint ").append(ckName);
    buffer.append(" ").append(checkConstraint);
  }

  protected void writeCompoundUniqueConstraints(DdlBuffer apply, CreateTable createTable) {
    //TODO: Write compound unique constraints
  }

  /**
   * Write the unique constraints inline with the create table statement.
   */
  protected void writeUniqueConstraints(DdlBuffer apply, CreateTable createTable) throws IOException {

    List<Column> columns = createTable.getColumn();
    for (Column column : columns) {
      if (isTrue(column.isUnique())) {
        inlineUniqueConstraintSingle(apply, createTable.getName(), column);
        indexSet.add(column);
      }
    }
  }

  /**
   * Write the unique constraint inline with the create table statement.
   */
  protected void inlineUniqueConstraintSingle(DdlBuffer buffer, String tableName, Column column) throws IOException {

    String uqName = determineUniqueConstraintName(tableName, column.getName());

    buffer.append(",").newLine();
    buffer.append("  constraint ").append(uqName).append(" unique ");
    buffer.append("(");
    buffer.append(lowerName(column.getName()));
    buffer.append(")");
  }

  /**
   * Write the primary key constraint inline with the create table statement.
   */
  protected void writePrimaryKeyConstraint(DdlBuffer buffer, String tableName, String[] pkColumns) throws IOException {

    String pkName = determinePrimaryKeyName(tableName);

    buffer.append(",").newLine();
    buffer.append("  constraint ").append(pkName).append(" primary key");
    appendColumns(pkColumns, buffer);
  }

  /**
   * Write alter table add primary key statement.
   */
  public void alterTableAddPrimaryKey(DdlBuffer buffer, String tableName, List<Column> pk) throws IOException {

    String[] pkColumns = toColumnNames(pk);
    String pkName = determinePrimaryKeyName(tableName);

    buffer.append("alter table ").append(tableName);
    buffer.append(" add primary key ").append(pkName);
    appendColumns(pkColumns, buffer);
    buffer.append(")").endOfStatement();
  }

  /**
   * Return as an array of string column names.
   */
  private String[] toColumnNames(List<Column> columns) {

    String[] cols = new String[columns.size()];
    for (int i = 0; i < cols.length; i++) {
      cols[i] = columns.get(i).getName();
    }
    return cols;
  }

  /**
   * Return as an array of string column names.
   */
  private String[] toColumnNamesSplit(String columns) {
    return columns.split(",");
  }

  /**
   * Convert the table or column name to lower case.
   * <p>
   * This is passed up to the platformDdl to override as desired.
   * Generally lower case with underscore is a good cross database
   * choice for column/table names.
   */
  protected String lowerName(String name) {
    return platformDdl.lowerName(name);
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, Column column, boolean useIdentity) throws IOException {

    boolean identityColumn = useIdentity && isTrue(column.isPrimaryKey());
    String platformType = convertToPlatformType(column.getType(), identityColumn);

    buffer.append("  ");
    buffer.append(lowerName(column.getName()), 30);
    buffer.append(platformType);
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      buffer.append(" not null");
    }

    // add check constraints later as we really want to give them a nice name
    // so that the database can potentially provide a nice SQL error
  }

  /**
   * Convert the expected logical type into a platform specific one.
   * <p>
   * For example clob -> text for postgres.
   * </p>
   */
  protected String convertToPlatformType(String type, boolean identity) {
    return platformDdl.convert(type, identity);
  }

  /**
   * Return the primary key constraint name.
   */
  protected String determinePrimaryKeyName(String tableName) {

    return namingConvention.primaryKeyName(tableName);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String determineForeignKeyConstraintName(String tableName, String columnName) {

    return namingConvention.foreignKeyConstraintName(tableName, columnName, ++countForeignKey);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String determineForeignKeyIndexName(String tableName, String[] columns) {

    return namingConvention.foreignKeyIndexName(tableName, columns, ++countIndex);
  }

  /**
   * Return the unique constraint name.
   */
  protected String determineUniqueConstraintName(String tableName, String columnName) {

    return namingConvention.uniqueConstraintName(tableName, columnName, ++countUnique);
  }

  /**
   * Return the constraint name.
   */
  protected String determineCheckConstraintName(String tableName, String columnName) {

    return namingConvention.checkConstraintName(tableName, columnName, ++countCheck);
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

  private int toInt(BigInteger value) {
    return (value == null) ? 0 : value.intValue();
  }


  /**
   * The indexes held on the table.
   * <p>
   * Used to detect when we don't need to add an index on the foreign key columns
   * when there is an existing unique constraint with the same columns.
   */
  protected static class IndexSet {

    private List<IndexColumns> indexes = new ArrayList<IndexColumns>();

    /**
     * Clear the indexes (for each table).
     */
    public void clear() {
      indexes.clear();
    }

    /**
     * Add an index for the given column.
     */
    public void add(Column column) {
      indexes.add(new IndexColumns(column));
    }

    /**
     * Return true if an index should be added for the given columns.
     * <p>
     * Returning false indicates there is an existing index (unique constraint) with these columns
     * and that an extra index should not be added.
     * </p>
     */
    public boolean add(String[] columns) {
      IndexColumns newIndex = new IndexColumns(columns);
      for (int i = 0; i <indexes.size() ; i++) {
        if (indexes.get(i).isMatch(newIndex)) {
          return false;
        }
      }
      indexes.add(newIndex);
      return true;
    }
  }

  /**
   * Set of columns making up a particular index (column order is important).
   */
  protected static class IndexColumns {

    List<String> columns = new ArrayList<String>(4);

    /**
     * Construct representing as a single column index.
     */
    public IndexColumns(Column column) {
      columns.add(column.getName());
    }

    /**
     * Construct representing index.
     */
    public IndexColumns(String[] columnNames) {
      for (int i = 0; i <columnNames.length; i++) {
        columns.add(columnNames[i]);
      }
    }

    /**
     * Return true if there this index match (same columns same order).
     */
    public boolean isMatch(IndexColumns other) {
      return columns.equals(other.columns);
    }

    protected void add(String column) {
      columns.add(column);
    }
  }
}
