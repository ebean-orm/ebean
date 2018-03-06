package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.ForeignKey;

class WriteForeignKey {

  private final String fkName;
  private final String tableName;
  private final String[] cols;
  private String refTableName;
  private String[] refCols;
  private final String indexName;
  private final ConstraintMode onDelete;
  private final ConstraintMode onUpdate;

  WriteForeignKey(AlterColumn alterColumn) {
    this.tableName = alterColumn.getTableName();
    this.indexName = alterColumn.getForeignKeyIndex();
    this.fkName = alterColumn.getForeignKeyName();
    this.cols = new String[]{alterColumn.getColumnName()};
    setReferences(alterColumn.getReferences());
    this.onDelete = modeOf(alterColumn.getForeignKeyOnDelete());
    this.onUpdate = modeOf(alterColumn.getForeignKeyOnUpdate());
  }

  WriteForeignKey(String tableName, ForeignKey key) {
    this.tableName = tableName;
    this.indexName = key.getIndexName();
    this.fkName = key.getName();
    this.cols = toCols(key.getColumnNames());
    this.refTableName = key.getRefTableName();
    this.refCols = toCols(key.getRefColumnNames());
    this.onDelete = modeOf(key.getOnDelete());
    this.onUpdate = modeOf(key.getOnUpdate());
  }

  WriteForeignKey(AlterForeignKey key) {
    this.tableName = key.getTableName();
    this.indexName = key.getIndexName();
    this.fkName = key.getName();
    this.cols = toCols(key.getColumnNames());
    this.refTableName = key.getRefTableName();
    this.refCols = toCols(key.getRefColumnNames());
    this.onDelete = modeOf(key.getOnDelete());
    this.onUpdate = modeOf(key.getOnUpdate());
  }

  WriteForeignKey(String tableName, Column column) {
    this.tableName = tableName;
    this.indexName = column.getForeignKeyIndex();
    this.fkName = column.getForeignKeyName();
    this.cols = new String[]{column.getName()};
    setReferences(column.getReferences());
    this.onDelete = modeOf(column.getForeignKeyOnDelete());
    this.onUpdate = modeOf(column.getForeignKeyOnUpdate());
  }

  private void setReferences(String references) {
    int pos = references.lastIndexOf('.');
    if (pos == -1) {
      throw new IllegalStateException("Expecting period '.' character for table.column split but not found in [" + references + "]");
    }
    this.refTableName = references.substring(0, pos);
    String refColumnName = references.substring(pos + 1);
    this.refCols = new String[]{refColumnName};
  }

  private String[] toCols(String columns) {
    return SplitColumns.split(columns);
  }

  private ConstraintMode modeOf(String value) {
    return (value == null) ? null : ConstraintMode.valueOf(value);
  }

  public String table() {
    return tableName;
  }

  public String[] cols() {
    return cols;
  }

  public String indexName() {
    return indexName;
  }

  public String fkName() {
    return fkName;
  }

  public String refTable() {
    return refTableName;
  }

  public String[] refCols() {
    return refCols;
  }

  public ConstraintMode onDelete() {
    return onDelete;
  }

  public ConstraintMode onUpdate() {
    return onUpdate;
  }

}
