package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.migration.CreateIndex;

import static io.ebeaninternal.dbmigration.ddlgeneration.platform.SplitColumns.split;

class WriteCreateIndex {

  private final String indexName;
  private final String tableName;
  private final String[] columns;
  private final boolean unique;
  private final boolean concurrent;
  private final String definition;

  WriteCreateIndex(String indexName, String tableName, String[] columns, boolean unique) {
    this.indexName = indexName;
    this.tableName = tableName;
    this.columns = columns;
    this.unique = unique;
    this.concurrent = false;
    this.definition = null;
  }

  public WriteCreateIndex(CreateIndex index) {
    this.indexName = index.getIndexName();
    this.tableName = index.getTableName();
    this.columns = split(index.getColumns());
    this.unique = Boolean.TRUE.equals(index.isUnique());
    this.concurrent = Boolean.TRUE.equals(index.isConcurrent());
    this.definition = index.getDefinition();
  }

  public String getIndexName() {
    return indexName;
  }

  public String getTableName() {
    return tableName;
  }

  public String[] getColumns() {
    return columns;
  }

  public boolean isUnique() {
    return unique;
  }

  public boolean isConcurrent() {
    return concurrent;
  }

  public String getDefinition() {
    return definition;
  }

  public boolean useDefinition() {
    return definition != null && !definition.isEmpty();
  }
}
