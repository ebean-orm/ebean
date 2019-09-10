package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.ChangeSetType;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.Migration;

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
  private final List<Object> applyChanges = new ArrayList<>();

  /**
   * List of 'drop' type changes. Expected to be placed into a separate DDL script.
   */
  private final List<Object> dropChanges = new ArrayList<>();

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
    if (!applyChanges.isEmpty()) {
      // add a non empty apply changeSet
      migration.getChangeSet().add(getApplyChangeSet());
    }

    if (!dropChanges.isEmpty()) {
      // add a non empty drop changeSet
      migration.getChangeSet().add(getDropChangeSet());
    }
    return migration;
  }

  /**
   * Return the list of 'apply' changes.
   */
  List<Object> getApplyChanges() {
    return applyChanges;
  }

  /**
   * Return the list of 'drop' changes.
   */
  List<Object> getDropChanges() {
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
  ChangeSet getDropChangeSet() {
    // put the changes into a ChangeSet
    ChangeSet createChangeSet = new ChangeSet();
    createChangeSet.setType(ChangeSetType.PENDING_DROPS);
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

    // register un-applied ones from the previous migrations
    baseModel.registerPendingHistoryDropColumns(newModel);
    if (!dropChanges.isEmpty()) {
      // register new ones created just now as part of this diff
      newModel.registerPendingHistoryDropColumns(getDropChangeSet());
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

  /**
   * Add a table comment to the 'apply' changes.
   */
  public void addTableComment(AddTableComment addTableComment) {
    applyChanges.add(addTableComment);
  }

  /**
   * Adds (or drops) a unique constraint to the 'apply' changes.
   */
  public void addUniqueConstraint(AddUniqueConstraint addUniqueConstraint) {
    applyChanges.add(addUniqueConstraint);
  }

  /**
   * Adds (or drops) a foreign key constraint to the 'apply' changes.
   */
  public void addAlterForeignKey(AlterForeignKey alterForeignKey) {
    applyChanges.add(alterForeignKey);
  }
}
