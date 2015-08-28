package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.CreateIndex;
import com.avaje.ebean.dbmigration.migration.DropIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Index as part of the logical model.
 */
public class MIndex {

  private String tableName;

  private String indexName;

  private List<String> columns = new ArrayList<String>();

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
    for (int i = 0; i < columnNames.length; i++) {
      this.columns.add(columnNames[i]);
    }
  }

  public MIndex(CreateIndex createIndex) {
    this.indexName = createIndex.getIndexName();
    this.tableName = createIndex.getTableName();
    this.columns = split(createIndex.getColumns());
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
    return create;
  }

  /**
   * Create a DropIndex migration for this index.
   */
  public DropIndex dropIndex() {
    DropIndex dropIndex = new DropIndex();
    dropIndex.setIndexName(indexName);
    dropIndex.setTableName(tableName);
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

    List<String> colList = new ArrayList<String>();
    String[] cols = columns.split(",");
    for (int i = 0; i <cols.length; i++) {
      colList.add(cols[i]);
    }
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
