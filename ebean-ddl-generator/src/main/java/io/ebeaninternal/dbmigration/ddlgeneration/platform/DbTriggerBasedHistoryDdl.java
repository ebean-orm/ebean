package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlAlterTable;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.List;

/**
 * Uses DB triggers to maintain a history table.
 */
public abstract class DbTriggerBasedHistoryDdl extends DbTableBasedHistoryDdl implements PlatformHistoryDdl {

  protected String sysPeriod;
  protected String sysPeriodStart;
  protected String sysPeriodEnd;

  protected String viewSuffix;


  protected String sysPeriodType = "datetime(6)";
  protected String now = "now(6)";
  protected String sysPeriodEndValue = "now(6)";

  DbTriggerBasedHistoryDdl() {
  }

  @Override
  public void configure(DatabaseConfig config, PlatformDdl platformDdl) {
    super.configure(config, platformDdl);
    this.sysPeriod = config.getAsOfSysPeriod();
    this.viewSuffix = config.getAsOfViewSuffix();

    this.sysPeriodStart = sysPeriod + "_start";
    this.sysPeriodEnd = sysPeriod + "_end";
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {

    String baseTable = dropHistoryTable.getBaseTable();

    // drop in appropriate order
    dropTriggers(writer.applyDropDependencies(), baseTable);
    dropWithHistoryView(writer.applyDropDependencies(), baseTable);
    dropHistoryTable(writer.applyDropDependencies(), baseTable);

    dropSysPeriodColumns(writer, baseTable);
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {

    String baseTable = addHistoryTable.getBaseTable();
    MTable table = writer.getTable(baseTable);
    if (table == null) {
      throw new IllegalStateException("MTable " + baseTable + " not found in writer? (required for history DDL)");
    }

    createWithHistory(writer, table);
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) {

    String baseTable = table.getName();

    addSysPeriodColumns(writer, baseTable, table.getWhenCreatedColumn());
    createHistoryTable(writer.applyPostAlter(), table);

    createWithHistoryView(writer.applyPostAlter(), table.getName());
    createTriggers(writer.applyPostAlter(), baseTable, columnNamesForApply(table));
    writer.applyPostAlter().end();

    // drop all scripts
    dropTriggers(writer.dropAll(), baseTable);
    dropWithHistoryView(writer.dropAll(), baseTable);
    dropHistoryTable(writer.dropAll(), baseTable);
    // no need to dropSysPeriodColumns as whole table will be deleted soon

  }

  @Override
  public void updateTriggers(DdlWrite writer, String tableName) {
    MTable table = writer.getTable(tableName);
    if (table != null && table.isWithHistory()) {
      DdlAlterTable alter = platformDdl.alterTable(writer, tableName);
      if (!alter.isHistoryHandled()) {
        // this code effectively disables history support before the table alter and enables it again
        // immediately after the table alter. As all alters per table are altogether now, this can done here
        dropTriggers(writer.apply(), tableName);
        dropWithHistoryView(writer.apply(), tableName);
        // here are the alter commands
        createWithHistoryView(writer.applyPostAlter(), tableName);
        createTriggers(writer.applyPostAlter(), tableName, columnNamesForApply(table));

        alter.setHistoryHandled();
      }
    }
  }

  /**
   * Will add a history trigger to the buffer. The config
   * 
   * @param buffer
   * @param table
   */
  protected abstract void createTriggers(DdlBuffer buffer, String baseTable, List<String> columnNames);

  protected abstract void dropTriggers(DdlBuffer buffer, String baseTable);

  protected String historyViewName(String baseTableName) {
    return normalise(baseTableName, viewSuffix);
  }

  protected String procedureName(String baseTableName) {
    return normalise(baseTableName, "_history_version");
  }

  protected String triggerName(String baseTableName) {
    return normalise(baseTableName, "_history_upd");
  }

  protected String updateTriggerName(String baseTableName) {
    return normalise(baseTableName, "_history_upd");
  }

  protected String deleteTriggerName(String baseTableName) {
    return normalise(baseTableName, "_history_del");
  }

  protected void addSysPeriodColumns(DdlWrite writer, String baseTableName, String whenCreatedColumn) {

    platformDdl.alterTableAddColumn(writer, baseTableName, sysPeriodStart, sysPeriodType, now);
    platformDdl.alterTableAddColumn(writer, baseTableName, sysPeriodEnd, sysPeriodType, null);
    if (whenCreatedColumn != null) {
      writer.applyPostAlter()
        .append("update ").append(baseTableName).append(" set ").append(sysPeriodStart).append(" = ").append(whenCreatedColumn).endOfStatement();
    }
  }

  protected void createHistoryTable(DdlBuffer apply, MTable table) {
    createHistoryTableAs(apply, table);
    createHistoryTableWithPeriod(apply);
  }

  protected void createHistoryTableAs(DdlBuffer apply, MTable table) {
    apply.append(platformDdl.getCreateTableCommandPrefix()).append(" ").append(historyTableName(table.getName())).append("(").newLine();
    for (MColumn column : table.allColumns()) {
      if (!column.isDraftOnly()) {
        writeColumnDefinition(apply, column.getName(), column.getType());
        apply.append(",").newLine();
      }
    }
    // TODO: We must apply also pending dropped columns. Let's do that in a later step
    if (table.hasDroppedColumns()) {
      throw new IllegalStateException(table.getName() + " has dropped columns. Please generate drop script before enabling history");
    }
  }

  protected void createHistoryTableWithPeriod(DdlBuffer apply) {
    writeColumnDefinition(apply, sysPeriodStart, sysPeriodType);
    apply.append(",").newLine();
    writeColumnDefinition(apply, sysPeriodEnd, sysPeriodType);
    apply.newLine().append(")").endOfStatement();
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, String columnName, String type) {

    String platformType = platformDdl.convert(type);
    buffer.append("  ");
    buffer.append(quote(columnName), 29);
    buffer.append(platformType);
  }

  protected void createWithHistoryView(DdlBuffer apply, String baseTableName) {

    apply
      .append("create view ").append(historyViewName(baseTableName))
      .append(" as select * from ").append(quote(baseTableName))
      .append(" union all select * from ").append(historyTableName(baseTableName))
      .endOfStatement();
  }

  protected void appendSysPeriodColumns(DdlBuffer apply, String prefix) {
    appendColumnName(apply, prefix, sysPeriodStart);
    appendColumnName(apply, prefix, sysPeriodEnd);
  }

  protected void dropWithHistoryView(DdlBuffer apply, String baseTableName) {
    apply.append("drop view ").append(historyViewName(baseTableName)).endOfStatement();
  }

  protected void dropHistoryTable(DdlBuffer apply, String baseTableName) {
    apply.append("drop table ").append(historyTableName(baseTableName)).endOfStatement().end();
  }

  protected void dropSysPeriodColumns(DdlWrite writer, String baseTableName) {
    platformDdl.alterTableDropColumn(writer, baseTableName, sysPeriodStart);
    platformDdl.alterTableDropColumn(writer, baseTableName, sysPeriodEnd);
  }

  protected void appendInsertIntoHistory(DdlBuffer buffer, String baseTable, List<String> columns) {

    buffer.append("    insert into ").append(historyTableName(baseTable)).append(" (").append(sysPeriodStart).append(",").append(sysPeriodEnd).append(",");
    appendColumnNames(buffer, columns, "");
    buffer.append(") values (OLD.").append(sysPeriodStart).append(", ").append(sysPeriodEndValue).append(",");
    appendColumnNames(buffer, columns, "OLD.");
    buffer.append(");").newLine();
  }

  void appendColumnNames(DdlBuffer buffer, List<String> columns, String columnPrefix) {
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      buffer.append(columnPrefix);
      buffer.append(columns.get(i));
    }
  }

  /**
   * Append a single column to the buffer if it is not null.
   */
  void appendColumnName(DdlBuffer buffer, String prefix, String columnName) {
    if (columnName != null) {
      buffer.append(prefix).append(columnName);
    }
  }

  /**
   * Return the column names included in history for the apply script.
   * <p>
   * Note that dropped columns are actually still included at this point as they are going
   * to be removed from the history handling when the drop script runs that also deletes
   * the column.
   * </p>
   */
  List<String> columnNamesForApply(MTable table) {
    return table.allHistoryColumns(true);
  }


}
