package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.ddlgeneration.platform.BaseDdlBuffer;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;

/**
 * Write context holding the buffers for both apply and rollback DDL.
 */
public class DdlWrite {

  private final ModelContainer currentModel;

  private final DdlBuffer apply;

  private final DdlBuffer applyForeignKeys;

  private final DdlBuffer applyHistory;

  private final DdlBuffer rollbackForeignKeys;

  private final DdlBuffer rollbackLast;

  /**
   * Create without any configuration or current model (no history support).
   */
  public DdlWrite() {
    this(new MConfiguration(), new ModelContainer());
  }

  /**
   * Create with a configuration.
   */
  public DdlWrite(MConfiguration configuration, ModelContainer currentModel) {
    this.currentModel = currentModel;
    this.apply = new BaseDdlBuffer(configuration);
    this.applyForeignKeys = new BaseDdlBuffer(configuration);
    this.applyHistory = new BaseDdlBuffer(configuration);
    this.rollbackForeignKeys = new BaseDdlBuffer(configuration);
    this.rollbackLast = new BaseDdlBuffer(configuration);
  }

  /**
   * Return the Table information from the current model.
   * <p>
   * This is typically required for the history support (used to determine the list of columns
   * included in the history when creating or recreating the associated trigger/stored procedure).
   * </p>
   */
  public MTable getTable(String tableName) {
    return currentModel.getTable(tableName);
  }

  /**
   * Return the buffer that APPLY DDL is written to.
   */
  public DdlBuffer apply() {
    return apply;
  }

  /**
   * Return the buffer that APPLY DDL is written to for foreign keys and their associated indexes.
   * <p>
   * Statements added to this buffer are executed after all the normal apply statements and
   * typically 'add foreign key' is added to this buffer.
   */
  public DdlBuffer applyForeignKeys() {
    return applyForeignKeys;
  }

  /**
   * Return the buffer that apply history DDL is written to.
   */
  public DdlBuffer applyHistory() {
    return applyHistory;
  }

  /**
   * Return the buffer that ROLLBACK DDL is written to for foreign keys and associated indexes.
   */
  public DdlBuffer rollbackForeignKeys() {
    return rollbackForeignKeys;
  }

  /**
   * Return the buffer that ROLLBACK DDL is written to (typically drop tables).
   * <p>
   * Statements added to this rollback buffer are executed after foreign key rollback
   * has been executed.
   */
  public DdlBuffer rollback() {
    return rollbackLast;
  }


}
