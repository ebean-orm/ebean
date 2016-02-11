package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.ddlgeneration.platform.BaseDdlBuffer;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;

/**
 * Write context holding the buffers for both apply and rollback DDL.
 */
public class DdlWrite {

  public enum Mode {
    APPLY,
    ROLLBACK
  }

  private final ModelContainer currentModel;

  private final DdlBuffer applyDropDependencies;

  private final DdlBuffer apply;

  private final DdlBuffer applyForeignKeys;

  private final DdlBuffer applyHistory;

  private final DdlBuffer rollbackDropDependencies;

  private final DdlBuffer rollbackForeignKeys;

  private final DdlBuffer rollback;

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
    this.applyDropDependencies = new BaseDdlBuffer(configuration);
    this.apply = new BaseDdlBuffer(configuration);
    this.applyForeignKeys = new BaseDdlBuffer(configuration);
    this.applyHistory = new BaseDdlBuffer(configuration);
    this.rollbackDropDependencies = new BaseDdlBuffer(configuration);
    this.rollbackForeignKeys = new BaseDdlBuffer(configuration);
    this.rollback = new BaseDdlBuffer(configuration);
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
        && applyHistory.getBuffer().isEmpty()
        && applyDropDependencies.getBuffer().isEmpty();
  }

  /**
   * Return true if the apply rollback buffers are all empty.
   */
  public boolean isApplyRollbackEmpty() {
    return rollback.getBuffer().isEmpty()
        && rollbackForeignKeys.getBuffer().isEmpty()
        && rollbackDropDependencies.getBuffer().isEmpty();
  }

  /**
   * Return the apply or rollback buffer.
   */
  public DdlBuffer buffer(Mode mode) {
    switch (mode) {
      case APPLY: return apply();
      case ROLLBACK: return rollback();
      default:
        throw new IllegalStateException("Invalid mode" + mode);
    }
  }

  /**
   * Return the apply or rollback buffer.
   */
  public DdlBuffer historyBuffer(Mode mode) {
    switch (mode) {
      case APPLY: return applyHistory();
      case ROLLBACK: return rollback();
      default:
        throw new IllegalStateException("Invalid mode" + mode);
    }
  }

  /**
   * Return the apply or rollback drop dependencies buffer.
   */
  public DdlBuffer dropDependencies(Mode mode) {

    switch (mode) {
      case APPLY: return applyDropDependencies();
      case ROLLBACK: return rollbackDropDependencies();
      default:
        throw new IllegalStateException("Invalid mode" + mode);
    }
  }

  /**
   * Return the buffer that APPLY DDL is written to.
   */
  public DdlBuffer apply() {
    return apply;
  }

  /**
   * Return the buffer that executes early to drop dependencies like views etc.
   */
  public DdlBuffer applyDropDependencies() {
    return applyDropDependencies;
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
   * Return the buffer that rollback executes early to drop dependencies like views.
   */
  public DdlBuffer rollbackDropDependencies() {
    return rollbackDropDependencies;
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

  public DdlBuffer drop() {
    return apply;
  }

}
