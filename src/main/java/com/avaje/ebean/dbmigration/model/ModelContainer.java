package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.CreateIndex;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropIndex;
import com.avaje.ebean.dbmigration.migration.DropTable;
import com.avaje.ebean.dbmigration.migration.Migration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all the tables, views, indexes etc that represent the model.
 * <p>
 * Migration changeSets can be applied to the model.
 * </p>
 */
public class ModelContainer {

  /**
   * All the tables in the model.
   */
  private Map<String, MTable> tables = new LinkedHashMap<String, MTable>();

  /**
   * All the non unique non foreign key indexes.
   */
  private Map<String, MIndex> indexes = new LinkedHashMap<String, MIndex>();

  public ModelContainer() {

  }

  /**
   * Return the map of all the tables.
   */
  public Map<String, MTable> getTables() {
    return tables;
  }

  /**
   * Return the map of all the non unique non fk indexes.
   */
  public Map<String, MIndex> getIndexes() {
    return indexes;
  }

  /**
   * Return the table by name.
   */
  public MTable getTable(String tableName) {
    return tables.get(tableName);
  }

  /**
   * Return the index by name.
   */
  public MIndex getIndex(String indexName) {
    return indexes.get(indexName);
  }

  /**
   * Apply a migration with associated changeSets to the model.
   */
  public void apply(Migration migration) {

    List<ChangeSet> changeSets = migration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      applyChangeSet(changeSet);
    }
  }

  /**
   * Apply a changeSet to the model.
   */
  protected void applyChangeSet(ChangeSet changeSet) {

    List<Object> changeSetChildren = changeSet.getChangeSetChildren();
    for (Object change : changeSetChildren) {
      if (change instanceof CreateTable) {
        applyChange((CreateTable) change);
      } else if (change instanceof DropTable) {
        applyChange((DropTable) change);
      } else if (change instanceof AlterColumn) {
        applyChange((AlterColumn) change);
      } else if (change instanceof AddColumn) {
        applyChange((AddColumn) change);
      } else if (change instanceof DropColumn) {
        applyChange((DropColumn) change);
      } else if (change instanceof CreateIndex) {
        applyChange((CreateIndex) change);
      } else if (change instanceof DropIndex) {
        applyChange((DropIndex) change);
      } else if (change instanceof AddHistoryTable) {
        applyChange((AddHistoryTable) change);
      } else if (change instanceof DropHistoryTable) {
        applyChange((DropHistoryTable) change);
      }
    }
  }

  /**
   * Set the withHistory flag on the associated base table.
   */
  private void applyChange(AddHistoryTable change) {

    MTable table = tables.get(change.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("Table [" + change.getBaseTable() + "] does not exist in model?");
    }
    table.setWithHistory(true);
  }

  /**
   * Unset the withHistory flag on the associated base table.
   */
  private void applyChange(DropHistoryTable change) {

    MTable table = tables.get(change.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("Table [" + change.getBaseTable() + "] does not exist in model?");
    }
    table.setWithHistory(false);
  }

  /**
   * Apply a CreateTable change to the model.
   */
  protected void applyChange(CreateTable createTable) {
    String tableName = createTable.getName();
    if (tables.containsKey(tableName)) {
      throw new IllegalStateException("Table [" + tableName + "] already exists in model?");
    }
    MTable table = new MTable(createTable);
    tables.put(tableName, table);
  }

  /**
   * Apply a DropTable change to the model.
   */
  protected void applyChange(DropTable dropTable) {
    String tableName = dropTable.getName();
    if (!tables.containsKey(tableName)) {
      throw new IllegalStateException("Table [" + tableName + "] does not exists in model?");
    }
    tables.remove(tableName);
  }

  /**
   * Apply a CreateTable change to the model.
   */
  protected void applyChange(CreateIndex createIndex) {
    String indexName = createIndex.getIndexName();
    if (indexes.containsKey(indexName)) {
      throw new IllegalStateException("Index [" + indexName + "] already exists in model?");
    }
    MIndex index = new MIndex(createIndex);
    indexes.put(createIndex.getIndexName(), index);
  }

  /**
   * Apply a DropTable change to the model.
   */
  protected void applyChange(DropIndex dropIndex) {
    String name = dropIndex.getIndexName();
    if (!indexes.containsKey(name)) {
      throw new IllegalStateException("Index [" + name + "] does not exist in model?");
    }
    indexes.remove(name);
  }


  /**
   * Apply a AddColumn change to the model.
   */
  protected void applyChange(AddColumn addColumn) {
    MTable table = tables.get(addColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + addColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(addColumn);
  }

  /**
   * Apply a AddColumn change to the model.
   */
  protected void applyChange(AlterColumn alterColumn) {
    MTable table = tables.get(alterColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + alterColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(alterColumn);
  }

  /**
   * Apply a DropColumn change to the model.
   */
  protected void applyChange(DropColumn dropColumn) {
    MTable table = tables.get(dropColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + dropColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(dropColumn);
  }

  /**
   * Add a table (typically from reading EbeanServer meta data).
   */
  public void addTable(MTable table) {
    tables.put(table.getName(), table);
  }

  /**
   * Add a single column index.
   */
  public void addIndex(String indexName, String tableName, String columnName) {

    indexes.put(indexName, new MIndex(indexName, tableName, columnName));
  }

  /**
   * Add a multi column index.
   */
  public void addIndex(String indexName, String tableName, String[] columnNames) {

    indexes.put(indexName, new MIndex(indexName, tableName, columnNames));
  }
}
