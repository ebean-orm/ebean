package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.DropIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Index as part of the logical model.
 */
public class MIndex {

  private String tableName;
  private String indexName;
  private String platforms;
  private List<String> columns = new ArrayList<>();
  private boolean unique;
  private boolean concurrent;
  private String definition;

  /**
   * Create a single column non unique index.
   */
  public MIndex(String indexName, String tableName, String columnName) {
    this.tableName = tableName;
    this.indexName = indexName;
    this.columns.add(columnName);
  }

  /**
   * Create a multi column non unique index.
   */
  public MIndex(String indexName, String tableName, String[] columnNames) {
    this.tableName = tableName;
    this.indexName = indexName;
    Collections.addAll(this.columns, columnNames);
  }

  public MIndex(String indexName, String tableName, String[] columnNames, String platforms, boolean unique, boolean concurrent, String definition) {
    this(indexName, tableName, columnNames);
    this.platforms = platforms;
    this.unique = unique;
    this.concurrent = concurrent;
    this.definition = emptyToNull(definition);
  }

  public MIndex(CreateIndex createIndex) {
    this.indexName = createIndex.getIndexName();
    this.tableName = createIndex.getTableName();
    this.columns = split(createIndex.getColumns());
    this.platforms = createIndex.getPlatforms();
    this.unique = Boolean.TRUE.equals(createIndex.isUnique());
    this.concurrent = Boolean.TRUE.equals(createIndex.isConcurrent());
    this.definition = emptyToNull(createIndex.getDefinition());
  }

  public String getKey() {
    // currently indexName should be unique (not indexName + platforms)
    return indexName;
  }

  /**
   * Return the index name.
   */
  public String getIndexName() {
    return indexName;
  }

  /**
   * Return the table this index is on.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Return the columns in the index (in order).
   */
  public List<String> getColumns() {
    return columns;
  }

  /**
   * Return a CreateIndex migration for this index.
   */
  public CreateIndex createIndex() {
    CreateIndex create = new CreateIndex();
    create.setIndexName(indexName);
    create.setTableName(tableName);
    create.setColumns(join());
    create.setPlatforms(platforms);
    if (Boolean.TRUE.equals(unique)) {
      create.setUnique(Boolean.TRUE);
    }
    if (Boolean.TRUE.equals(concurrent)) {
      create.setConcurrent(Boolean.TRUE);
    }
    create.setDefinition(emptyToNull(definition));
    return create;
  }

  private String emptyToNull(String val) {
    if (val == null || val.isEmpty()) {
      return null;
    }
    return val;
  }

  /**
   * Create a DropIndex migration for this index.
   */
  public DropIndex dropIndex() {
    DropIndex dropIndex = new DropIndex();
    dropIndex.setIndexName(indexName);
    dropIndex.setTableName(tableName);
    dropIndex.setPlatforms(platforms);
    if (Boolean.TRUE.equals(concurrent)) {
      dropIndex.setConcurrent(Boolean.TRUE);
    }
    return dropIndex;
  }

  /**
   * Compare with an index of the same name.
   */
  public void compare(ModelDiff modelDiff, MIndex newIndex) {
    if (changed(newIndex)) {
      // drop and recreate the index
      modelDiff.addDropIndex(dropIndex());
      modelDiff.addCreateIndex(newIndex.createIndex());
    }
  }

  /**
   * Return true if the index has changed.
   */
  private boolean changed(MIndex newIndex) {
    if (!tableName.equals(newIndex.getTableName())) {
      return true;
    }
    if (unique != newIndex.unique) {
      return true;
    }
    if (!Objects.equals(definition, newIndex.definition)) {
      return true;
    }
    List<String> newColumns = newIndex.getColumns();
    if (columns.size() != newColumns.size()) {
      return true;
    }
    for (int i = 0; i < columns.size(); i++) {
      if (!columns.get(i).equals(newColumns.get(i))) {
        return true;
      }
    }
    return false;
  }


  private List<String> split(String columns) {
    if (columns.isEmpty()) {
      return Collections.emptyList();
    }
    String[] cols = columns.split(",");
    List<String> colList = new ArrayList<>(cols.length);
    Collections.addAll(colList, cols);
    return colList;
  }

  private String join() {
    StringBuilder sb = new StringBuilder(50);
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns.get(i));
    }
    return sb.toString();
  }

}
