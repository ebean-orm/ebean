package com.avaje.ebean.dbmigration.model;

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
  protected final ModelContainer baseModel;

  /**
   * List of 'create' type changes.
   */
  protected final List<Object> createChanges = new ArrayList<Object>();

  /**
   * List of 'drop' type changes. Potential for putting into a separate changeSet.
   */
  protected final List<Object> dropChanges = new ArrayList<Object>();

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
//    createChanges.add(newTable.createForeignKeys());
  }

  /**
   * Compare tables looking for add/drop/modify columns etc.
   */
  protected void compareTables(MTable currentTable, MTable newTable) {

    //TODO: compareTables()
    // changed columns
    // find additional columns
    // find removed columns
    // changes to indexes?
    // changes to primary key
    // changes to foreign key
    // changes to unique constraints?

  }
}
