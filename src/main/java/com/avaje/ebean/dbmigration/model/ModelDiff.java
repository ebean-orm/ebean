package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.ChangeSetType;
import com.avaje.ebean.dbmigration.migration.CreateIndex;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropIndex;
import com.avaje.ebean.dbmigration.migration.Migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to prepare a diff in terms of changes required to migrate from
 * the base model to the newer model.
 */
public class ModelDiff {

  /**
   * The base model to which we compare the newer model.
   */
  private final ModelContainer baseModel;

  /**
   * List of 'create' type changes.
   */
  private final List<Object> applyChanges = new ArrayList<Object>();

  /**
   * List of 'drop' type changes. Expected to be placed into a separate DDL script.
   */
  private final List<Object> dropChanges = new ArrayList<Object>();

  /**
   * Construct with a base model.
   */
  public ModelDiff(ModelContainer baseModel) {
    this.baseModel = baseModel;
  }

  /**
   * Construct with a base model.
   */
  public ModelDiff() {
    this.baseModel = new ModelContainer();
  }


  /**
   * Return true if the apply and drop changes are both empty.
   * This means there are no migration changes.
   */
  public boolean isEmpty() {
    return applyChanges.isEmpty() && dropChanges.isEmpty();
  }

  /**
   * Return the diff as a migration potentially containing
   * an apply changeSet and a drop changeSet.
   */
  public Migration getMigration() {

    Migration migration = new Migration();
    ChangeSet applyChangeSet = getApplyChangeSet();
    if (!applyChangeSet.getChangeSetChildren().isEmpty()) {
      // add a non empty apply changeSet
      migration.getChangeSet().add(applyChangeSet);
    }

    ChangeSet dropChangeSet = getDropChangeSet();
    if (!dropChangeSet.getChangeSetChildren().isEmpty()) {
      // add a non empty drop changeSet
      migration.getChangeSet().add(dropChangeSet);
    }
    return migration;
  }

  /**
   * Return the list of 'apply' changes.
   */
  public List<Object> getApplyChanges() {
    return applyChanges;
  }

  /**
   * Return the list of 'drop' changes.
   */
  public List<Object> getDropChanges() {
    return dropChanges;
  }

  /**
   * Return the 'apply' changeSet.
   */
  public ChangeSet getApplyChangeSet() {
    // put the changes into a ChangeSet
    ChangeSet applyChangeSet = new ChangeSet();
    applyChangeSet.setType(ChangeSetType.APPLY);
    applyChangeSet.getChangeSetChildren().addAll(applyChanges);
    return applyChangeSet;
  }

  /**
   * Return the 'drop' changeSet.
   */
  public ChangeSet getDropChangeSet() {
    // put the changes into a ChangeSet
    ChangeSet createChangeSet = new ChangeSet();
    createChangeSet.setType(ChangeSetType.DROP);
    createChangeSet.getChangeSetChildren().addAll(dropChanges);
    return createChangeSet;
  }

  /**
   * Compare to a 'newer' model and collect the differences.
   */
  public void compareTo(ModelContainer newModel) {

    Map<String, MTable> newTables = newModel.getTables();
    for (MTable newTable : newTables.values()) {

      MTable currentTable = baseModel.getTable(newTable.getName());
      if (currentTable == null) {
        addNewTable(newTable);
      } else {
        compareTables(currentTable, newTable);
      }
    }

    // search for tables that are no longer used
    for (MTable existingTable : baseModel.getTables().values()) {
      if (!newTables.containsKey(existingTable.getName())) {
        addDropTable(existingTable);
      }
    }

    Map<String, MIndex> newIndexes = newModel.getIndexes();
    for (MIndex newIndex : newIndexes.values()) {
      MIndex currentIndex = baseModel.getIndex(newIndex.getIndexName());
      if (currentIndex == null) {
        addCreateIndex(newIndex.createIndex());
      } else {
        compareIndexes(currentIndex, newIndex);
      }
    }

    // search for indexes that are no longer used
    for (MIndex existingIndex : baseModel.getIndexes().values()) {
      if (!newIndexes.containsKey(existingIndex.getIndexName())) {
        addDropIndex(existingIndex.dropIndex());
      }
    }

  }

  protected void addDropTable(MTable existingTable) {
    dropChanges.add(existingTable.dropTable());
  }

  /**
   * Add CreateTable to the 'apply' changes.
   */
  protected void addNewTable(MTable newTable) {
    applyChanges.add(newTable.createTable());
  }

  /**
   * Compare tables looking for add/drop/modify columns etc.
   */
  protected void compareTables(MTable currentTable, MTable newTable) {

    currentTable.compare(this, newTable);
  }

  /**
   * Compare tables looking for add/drop/modify columns etc.
   */
  protected void compareIndexes(MIndex currentIndex, MIndex newIndex) {

    currentIndex.compare(this, newIndex);
  }

  /**
   * Add the AlterColumn to the 'apply' changes.
   */
  public void addAlterColumn(AlterColumn alterColumn) {
    applyChanges.add(alterColumn);
  }

  /**
   * Add the AlterColumn to the 'apply' changes.
   */
  public void addAddColumn(AddColumn addColumn) {
    applyChanges.add(addColumn);
  }

  /**
   * Add the DropColumn to the 'drop' changes.
   */
  public void addDropColumn(DropColumn dropColumn) {
    dropChanges.add(dropColumn);
  }

  /**
   * Add the AddHistoryTable to apply changes.
   */
  public void addAddHistoryTable(AddHistoryTable addHistoryTable) {
    applyChanges.add(addHistoryTable);
  }

  /**
   * Add the DropHistoryTable to the 'drop history' changes.
   */
  public void addDropHistoryTable(DropHistoryTable dropHistoryTable) {
    dropChanges.add(dropHistoryTable);
  }

  /**
   * Add the DropIndex to the 'apply' changes.
   */
  public void addDropIndex(DropIndex dropIndex) {
    applyChanges.add(dropIndex);
  }

  /**
   * Add the CreateIndex to the 'apply' changes.
   */
  public void addCreateIndex(CreateIndex createIndex) {
    applyChanges.add(createIndex);
  }
}
