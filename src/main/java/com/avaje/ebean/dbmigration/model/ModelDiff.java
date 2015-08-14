package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.ChangeSetType;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.Migration;

import java.util.ArrayList;
import java.util.List;

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
  private final List<Object> createChanges = new ArrayList<Object>();

  /**
   * List of 'drop' type changes. Expected to be placed into a separate DDL script.
   */
  private final List<Object> dropChanges = new ArrayList<Object>();

  /**
   * List of 'drop' type changes. Expected to be placed into a separate DDL script.
   */
  private final List<Object> dropHistoryChanges = new ArrayList<Object>();

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
   * Return the list of 'create' changes.
   */
  public List<Object> getCreateChanges() {
    return createChanges;
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
    ChangeSet createChangeSet = new ChangeSet();
    createChangeSet.setType(ChangeSetType.APPLY);
    createChangeSet.getChangeSetChildren().addAll(createChanges);
    return createChangeSet;
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

    for (MTable newTable : newModel.getTables().values()) {

      MTable currentTable = baseModel.getTable(newTable.getName());
      if (currentTable == null) {
        addNewTable(newTable);
      } else {
        compareTables(currentTable, newTable);
      }
    }

    //TODO: other parts of the model? views, indexes etc

  }

  /**
   * Add CreateTable to the 'creation' changes.
   */
  protected void addNewTable(MTable newTable) {

    createChanges.add(newTable.createTable());
  }

  /**
   * Compare tables looking for add/drop/modify columns etc.
   */
  protected void compareTables(MTable currentTable, MTable newTable) {

    currentTable.compare(this, newTable);
  }

  /**
   * Add the AlterColumn to the 'apply' changes.
   */
  public void addAlterColumn(AlterColumn alterColumn) {
    createChanges.add(alterColumn);
  }

  /**
   * Add the AlterColumn to the 'apply' changes.
   */
  public void addAddColumn(AddColumn addColumn) {
    createChanges.add(addColumn);
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
    createChanges.add(addHistoryTable);
  }

  /**
   * Add the DropHistoryTable to the 'drop history' changes.
   */
  public void addDropHistoryTable(DropHistoryTable dropHistoryTable) {
    dropHistoryChanges.add(dropHistoryTable);
  }

}
