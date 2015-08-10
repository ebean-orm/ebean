package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.DropColumn;

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

  public void addAlterColumn(AlterColumn alterColumn) {
    createChanges.add(alterColumn);
  }

  public void addDropColumn(DropColumn dropColumn) {
    dropChanges.add(dropColumn);
  }

  public void addAddColumn(AddColumn addColumn) {
    createChanges.add(addColumn);
  }
}
