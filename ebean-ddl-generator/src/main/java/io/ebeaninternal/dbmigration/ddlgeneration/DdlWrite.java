package io.ebeaninternal.dbmigration.ddlgeneration;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import io.ebeaninternal.dbmigration.ddlgeneration.platform.BaseDdlBuffer;
import io.ebeaninternal.dbmigration.model.MConfiguration;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.ModelContainer;

/**
 * Write context holding the buffers for both apply and rollback DDL. Description of the apply buffers:
 * <ul>
 * <li><b>applyDropDependencies:</b> Contains drops for foreign keys, indices, constraint or drop history table</li>
 * <li><b>apply:</b> Contains &#64;DbMigraion.before, create table, create sequence or disable system versioning statements</li>
 * <li><b>applyAlterTables:</b> Contains table alters (only that change the table data structure, there may be table alters like
 * constraints etc. in postAlter)</li>
 * <li><b>applyPostAlter:</b> Contains check constraints, unique constraints (which CAN be an index), column and table comments,
 * &#64;DbMigraion.after, drop tables, drop sequences or enable system versioning statement</li>
 * <li><b>applyForeignKeys: Contains foreign keys and indices.</b>
 * <li><b>applyHistoryView:</b> The views for trigger based history support</li>
 * <li><b>applyHistoryTrigger:</b> The triggers for trigger based history support</li>
 * </ul>
 */
public class DdlWrite {

  private final ModelContainer currentModel;

  private final DdlBuffer applyDropDependencies = new BaseDdlBuffer();

  private final DdlBuffer apply = new BaseDdlBuffer();

  private final Map<String, DdlAlterTable> applyAlterTables = new TreeMap<>();

  private final DdlBuffer applyPostAlter = new BaseDdlBuffer();

  private final DdlBuffer applyForeignKeys = new BaseDdlBuffer();

  private final DdlBuffer applyHistoryView = new BaseDdlBuffer();

  private final DdlBuffer applyHistoryTrigger = new BaseDdlBuffer();

  private final DdlBuffer dropAllForeignKeys = new BaseDdlBuffer();

  private final DdlBuffer dropAll = new BaseDdlBuffer();

  private final DdlOptions options;

  /**
   * Create without any configuration or current model (no history support).
   */
  public DdlWrite() {
    this(new MConfiguration(), new ModelContainer(), new DdlOptions());
  }

  /**
   * Create with a configuration.
   */
  public DdlWrite(MConfiguration configuration, ModelContainer currentModel, DdlOptions options) {
    this.currentModel = currentModel;
    this.options = options;
  }

  /**
   * Return the DDL options.
   */
  public DdlOptions getOptions() {
    return options;
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
      && applyAlterTables.isEmpty()
      && applyPostAlter.getBuffer().isEmpty()
      && applyForeignKeys.getBuffer().isEmpty()
      && applyHistoryView.getBuffer().isEmpty()
      && applyHistoryTrigger.getBuffer().isEmpty()
      && applyDropDependencies.getBuffer().isEmpty();
  }

  /**
   * Return the buffer that POST ALTER is written to.
   */
  public DdlBuffer applyPostAlter() {
    return applyPostAlter;
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
   * Creates or returns the DdlAlterTable statement for <code>tablename</code>.
   * 
   * Note: All alters on a particular table are sorted in natural order in the ddl script. This allows optimizing the alters by
   * merging them or doing a reorg table after altering is done.
   * 
   * @param tableName the table name
   * @param factory   the factory to construct a new object
   * @return
   */
  public DdlAlterTable applyAlterTable(String tableName, Function<String, DdlAlterTable> factory) {
    return applyAlterTables.computeIfAbsent(tableName, factory);
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
   * Return the buffer that apply history-view DDL is written to.
   */
  public DdlBuffer applyHistoryView() {
    return applyHistoryView;
  }

  /**
   * Return the buffer that apply history-trigger DDL is written to.
   */
  public DdlBuffer applyHistoryTrigger() {
    return applyHistoryTrigger;
  }

  /**
   * Return the buffer used for the 'drop all DDL' for dropping foreign keys and associated indexes.
   */
  public DdlBuffer dropAllForeignKeys() {
    return dropAllForeignKeys;
  }

  /**
   * Return the buffer used for the 'drop all DDL' to drop tables, views and history triggers etc.
   */
  public DdlBuffer dropAll() {
    return dropAll;
  }

  /**
   * Writes the apply ddl to the target.
   */
  public void writeApply(Appendable target) throws IOException {
    if (!applyDropDependencies.isEmpty()) {
      target.append("-- drop dependencies\n");
      target.append(applyDropDependencies.getBuffer());
    }
    if (!apply.isEmpty()) {
      target.append("-- apply changes\n");
      target.append(apply.getBuffer());
    }
    if (!applyAlterTables.isEmpty()) {
      target.append("-- apply alter tables\n");
      for (DdlAlterTable alterTable : applyAlterTables.values()) {
        alterTable.write(target);
      }
    }
    if (!applyPostAlter.isEmpty()) {
      target.append("-- apply post alter\n");
      target.append(applyPostAlter.getBuffer());
    }
    if (!applyForeignKeys.isEmpty()) {
      target.append("-- foreign keys and indices\n");
      target.append(applyForeignKeys.getBuffer());
    }

    if (!applyHistoryView.isEmpty()) {
      target.append("-- apply history view\n");
      target.append(applyHistoryView.getBuffer());
    }

    if (!applyHistoryTrigger.isEmpty()) {
      target.append("-- apply history trigger\n");
      target.append(applyHistoryTrigger.getBuffer());
    }
  }

  /**
   * Writes the drop all ddl to the target.
   */
  public void writeDropAll(Appendable target) throws IOException {
    if (!dropAllForeignKeys.isEmpty()) {
      target.append("-- drop all foreign keys\n");
      target.append(dropAllForeignKeys.getBuffer());
    }
    if (!dropAll.isEmpty()) {
      target.append("-- drop all\n");
      target.append(dropAll.getBuffer());
    }
  }

  /**
   * Returns all create statements. Mainly used for unit-tests
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    try {
      writeDropAll(sb);
      writeApply(sb);
    } catch (IOException e) {
      // can not happen
    }
    return sb.toString();
  }

}
