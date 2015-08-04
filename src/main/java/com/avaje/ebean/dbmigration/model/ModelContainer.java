package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
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
   * Return the map of all the tables.
   */
  public Map<String, MTable> getTables() {
    return tables;
  }

  /**
   * Return the table by name.
   */
  public MTable getTable(String tableName) {
    return tables.get(tableName);
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
      } else if (change instanceof AddColumn) {
        applyChange((AddColumn) change);
      } else if (change instanceof DropColumn) {
        applyChange((DropColumn) change);
      }
    }
  }

  /**
   * Apply a CreateTable change to the model.
   */
  protected void applyChange(CreateTable createTable) {
    String tableName = createTable.getName();
    if (tables.containsKey(tableName)) {
      throw new IllegalStateException("Table [" + tableName + "] already exists?");
    }
    MTable table = new MTable(createTable);
    tables.put(tableName, table);
  }

  /**
   * Apply a AddColumn change to the model.
   */
  protected void applyChange(AddColumn addColumn) {
    MTable table = tables.get(addColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + addColumn.getTableName() + "] does not exist?");
    }
    table.apply(addColumn);
  }

  /**
   * Apply a DropColumn change to the model.
   */
  protected void applyChange(DropColumn dropColumn) {
    MTable table = tables.get(dropColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + dropColumn.getTableName() + "] does not exist?");
    }
    table.apply(dropColumn);
  }

  /**
   * Add a table (typically from reading EbeanServer meta data).
   */
  public void addTable(MTable table) {
    tables.put(table.getName(), table);
  }
}
