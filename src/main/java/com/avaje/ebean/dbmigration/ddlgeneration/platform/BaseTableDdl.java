package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.TableDdl;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation for 'create table' and 'alter table' statements.
 */
public class BaseTableDdl implements TableDdl {

  protected final DdlNamingConvention namingConvention;

  protected final PlatformDdl platformDdl;

  public BaseTableDdl(DdlNamingConvention namingConvention, PlatformDdl platformDdl) {
    this.namingConvention = namingConvention;
    this.platformDdl = platformDdl;
  }

  /**
   * Generate the appropriate 'create table' and matching 'drop table' statements
   * and add them to the 'apply' and 'rollback' buffers.
   */
  @Override
  public void generate(DdlWrite writer, CreateTable createTable) throws IOException {

    String tableName = createTable.getName();
    List<Column> columns = createTable.getColumn();
    List<Column> pk = determinePrimaryKeyColumns(columns);

    DdlBuffer apply = writer.apply();
    apply.append("create table ").append(tableName).append(" (");
    for (int i = 0; i < columns.size(); i++) {
      apply.newLine();
      writeColumnDefinition(apply, columns.get(i));
      if (i < columns.size() - 1) {
        apply.append(",");
      }
    }

    writeCheckConstraints(apply, createTable);
    writeUniqueConstraints(apply, createTable);
    writeCompoundUniqueConstraints(apply, createTable);
    if (!pk.isEmpty()) {
      writePrimaryKeyConstraint(apply, tableName, pk);
    }

    apply.newLine().append(")").endOfStatement();
    apply.end();

    writeAddForeignKeys(writer, createTable);

    // add drop table to the rollback buffer
    dropTable(writer.rollback(), tableName);

    if (isTrue(createTable.isWithHistory())) {
      createWithHistory(writer, createTable.getName());
    }

  }

  private void createWithHistory(DdlWrite writer, String name) throws IOException {

    MTable table = writer.getTable(name);
    platformDdl.createWithHistory(writer, table);
  }

  protected void writeAddForeignKeys(DdlWrite write, CreateTable createTable) throws IOException {

    List<Column> columns = createTable.getColumn();
    for (Column column : columns) {
      String references = column.getReferences();
      if (hasValue(references)) {
        writeForeignKey(write, createTable.getName(), column.getName(), references);
      }
    }
    //createTable.

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

    DdlBuffer fkeyBuffer = write.applyForeignKeys();
    fkeyBuffer
        .append("alter table ").append(tableName)
        .append(" add constraint ").append(fkName)
        .append(" foreign key (");
    appendColumns(columns, fkeyBuffer);
    fkeyBuffer
        .append(") references ")
        .append(refTable);
    appendColumns(refColumns, fkeyBuffer);
    fkeyBuffer.appendWithSpace(platformDdl.getForeignKeyRestrict())
        .endOfStatement();

    String indexName = determineForeignKeyIndexName(tableName, columns);

    fkeyBuffer.append("create index ").append(indexName).append(" on ").append(tableName);
    appendColumns(columns, fkeyBuffer);
    fkeyBuffer.endOfStatement();

    fkeyBuffer.end();

    write.rollbackForeignKeys()
        .append("drop index ").append(indexName)
        .endOfStatement();

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
      buffer.append(columns[i]);
    }
    buffer.append(")");
  }


  /**
   * Add 'drop table' statement to the buffer.
   */
  protected void dropTable(DdlBuffer buffer, String tableName) throws IOException {

    buffer.append("drop table ").append(tableName).endOfStatement().end();
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
    buffer.append(column.getName());
    buffer.append(")");
  }

  /**
   * Write the primary key constraint inline with the create table statement.
   */
  protected void writePrimaryKeyConstraint(DdlBuffer buffer, String tableName, List<Column> pk) throws IOException {

    String pkName = determinePrimaryKeyName(tableName, pk);

    buffer.append(",").newLine();
    buffer.append("  constraint ").append(pkName).append(" primary key ");
    buffer.append("(");
    for (int i = 0; i < pk.size(); i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(pk.get(i).getName());
    }
    buffer.append(")");
  }

  /**
   * Write alter table add primary key statement.
   */
  public void alterTableAddPrimaryKey(DdlBuffer buffer, String tableName, List<Column> pk) throws IOException {

    String pkName = determinePrimaryKeyName(tableName, pk);

    buffer.append("alter table ").append(tableName);
    buffer.append(" add primary key ").append(pkName).append(" (");
    for (int i = 0; i < pk.size(); i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(pk.get(i).getName());
    }
    buffer.append(")").endOfStatement();
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, Column column) throws IOException {

    String platformType = convertToPlatformType(column.getType(), isTrue(column.isIdentity()));

    buffer.append("  ");
    buffer.append(column.getName(), 30);
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
  protected String determinePrimaryKeyName(String tableName, List<Column> pkColumns) {

    // collect the primary key column names
    List<String> pkColumnNames = new ArrayList<String>(pkColumns.size());
    for (int i = 0; i < pkColumns.size(); i++) {
      pkColumnNames.add(pkColumns.get(i).getName());
    }
    return namingConvention.primaryKeyName(tableName, pkColumnNames);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String determineForeignKeyConstraintName(String tableName, String columnName) {

    return namingConvention.foreignKeyConstraintName(tableName, columnName);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String determineForeignKeyIndexName(String tableName, String[] columns) {

    return namingConvention.foreignKeyIndexName(tableName, columns);
  }

  /**
   * Return the unique constraint name.
   */
  protected String determineUniqueConstraintName(String tableName, String columnName) {

    return namingConvention.uniqueConstraintName(tableName, columnName);
  }

  /**
   * Return the constraint name.
   */
  protected String determineCheckConstraintName(String tableName, String columnName) {

    return namingConvention.checkConstraintName(tableName, columnName);
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


}
