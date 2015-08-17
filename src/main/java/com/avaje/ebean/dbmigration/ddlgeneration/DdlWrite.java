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

  private final DdlBuffer rollback;

  /**
   * For DDL that drops tables and columns etc.
   *
   * This DDL typically can not run automatically in production as there is most commonly
   * existing servers running the application using these tables and columns. Typically
   * these drop statements may be executed AFTER all the servers in the application have
   * migrated onto new code.
   */
  private final DdlBuffer drop;

  /**
   * For use when History is turned off for a base table or history is no longer
   * desired on specific columns. This DDL should typically execute manually after review
   * by DBA's.
   */
  private final DdlBuffer dropHistory;

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
    this.rollback = new BaseDdlBuffer(configuration);
    this.drop = new BaseDdlBuffer(configuration);
    this.dropHistory = new BaseDdlBuffer(configuration);
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
   * Return true if the apply buffers are all empty.
   */
  public boolean isApplyEmpty() {
    return apply.getBuffer().isEmpty()
        && applyForeignKeys.getBuffer().isEmpty()
        && applyHistory.getBuffer().isEmpty();
  }

  /**
   * Return true if the apply rollback buffers are all empty.
   */
  public boolean isApplyRollbackEmpty() {
    return rollback.getBuffer().isEmpty()
        && rollbackForeignKeys.getBuffer().isEmpty();
  }

  /**
   * Return true the drop buffers are empty.
   */
  public boolean isDropEmpty() {
    return drop.getBuffer().isEmpty() && dropHistory.getBuffer().isEmpty();
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
   * Return the buffer that ROLLBACK DDL is written to which is considered safe to run when
   * apply changes fail to execute. This will reverse the apply changes typically dropping
   * newly created tables, foreign keys etc.
   * <p>
   * When apply changes are made against DB's that support transactional DDL you could argue
   * that these rollback statements are not necessary.
   * <p>
   * Note that statements added to this rollback buffer are executed after foreign key rollback
   * has been executed.
   */
  public DdlBuffer rollback() {
    return rollback;
  }

  /**
   * Return the buffer that destructive changes are written to. This is typically drop table and
   * drop column.
   */
  public DdlBuffer drop() {
    return drop;
  }

  /**
   * Return the buffer that is used when history is no longer required on a table or specific columns.
   */
  public DdlBuffer dropHistory() {
    return dropHistory;
  }


}
