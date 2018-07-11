package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DbConstraintNaming;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Uses DB triggers to maintain a history table.
 */
public abstract class DbTriggerBasedHistoryDdl implements PlatformHistoryDdl {

  protected DbConstraintNaming constraintNaming;

  protected PlatformDdl platformDdl;

  protected String sysPeriod;
  protected String sysPeriodStart;
  protected String sysPeriodEnd;

  protected String viewSuffix;
  protected String historySuffix;


  protected String currentTimestamp = "now(6)";
  protected String sysPeriodType = "datetime(6)";

  public DbTriggerBasedHistoryDdl() {
  }

  @Override
  public void configure(ServerConfig serverConfig, PlatformDdl platformDdl) {
    this.platformDdl = platformDdl;
    this.sysPeriod = serverConfig.getAsOfSysPeriod();
    this.viewSuffix = serverConfig.getAsOfViewSuffix();
    this.historySuffix = serverConfig.getHistoryTableSuffix();
    this.constraintNaming = serverConfig.getConstraintNaming();

    this.sysPeriodStart = sysPeriod + "_start";
    this.sysPeriodEnd = sysPeriod + "_end";
  }

  @Override
  public void updateTriggers(DdlWrite writer, HistoryTableUpdate update) throws IOException {

    MTable table = writer.getTable(update.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("MTable " + update.getBaseTable() + " not found in writer? (required for history DDL)");
    }
    updateTriggers(writer, table, update);
  }

  /**
   * Replace the existing triggers/stored procedures/views for history table support given the included columns.
   */
  protected abstract void updateHistoryTriggers(DbTriggerUpdate triggerUpdate) throws IOException;

  /**
   * Process the HistoryTableUpdate which can result in changes to the apply, rollback
   * and drop scripts.
   */
  protected void updateTriggers(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException {

    writer.applyHistoryTrigger().append("-- changes: ").append(update.description()).newLine();

    updateHistoryTriggers(createDbTriggerUpdate(writer, table));
  }

  protected DbTriggerUpdate createDbTriggerUpdate(DdlWrite writer, MTable table) {

    List<String> columns = columnNamesForApply(table);
    String baseTableName = table.getName();
    String historyTableName = historyTableName(baseTableName);
    return new DbTriggerUpdate(baseTableName, historyTableName, writer, columns);
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {

    String baseTable = dropHistoryTable.getBaseTable();

    // drop in appropriate order
    dropTriggers(writer.applyDropDependencies(), baseTable);
    dropHistoryTableEtc(writer.applyDropDependencies(), baseTable);
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {

    String baseTable = addHistoryTable.getBaseTable();
    MTable table = writer.getTable(baseTable);
    if (table == null) {
      throw new IllegalStateException("MTable " + baseTable + " not found in writer? (required for history DDL)");
    }

    createWithHistory(writer, table);
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {

    String baseTable = table.getName();
    String whenCreatedColumn = table.getWhenCreatedColumn();

    dropTriggers(writer.dropAll(), baseTable);
    dropHistoryTableEtc(writer.dropAll(), baseTable);

    addHistoryTable(writer, table, whenCreatedColumn);
    createStoredFunction(writer, table);
    createTriggers(writer, table);
  }

  protected abstract void createTriggers(DdlWrite writer, MTable table) throws IOException;

  protected abstract void dropTriggers(DdlBuffer buffer, String baseTable) throws IOException;

  protected void createStoredFunction(DdlWrite writer, MTable table) throws IOException {
    // do nothing
  }

  protected String normalise(String tableName) {
    return constraintNaming.normaliseTable(tableName);
  }

  protected String historyTableName(String baseTableName) {
    return baseTableName + historySuffix;
  }

  protected String procedureName(String baseTableName) {
    return baseTableName + "_history_version";
  }

  protected String triggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_upd";
  }

  protected String updateTriggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_upd";
  }

  protected String deleteTriggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_del";
  }

  protected void addHistoryTable(DdlWrite writer, MTable table, String whenCreatedColumn) throws IOException {

    String baseTableName = table.getName();

    DdlBuffer apply = writer.applyHistoryView();

    addSysPeriodColumns(apply, baseTableName, whenCreatedColumn);
    createHistoryTable(apply, table);
    createWithHistoryView(apply, baseTableName);
  }

  protected void addSysPeriodColumns(DdlBuffer apply, String baseTableName, String whenCreatedColumn) throws IOException {

    apply.append("alter table ").append(baseTableName).append(" add column ")
      .append(sysPeriodStart).append(" ").append(sysPeriodType).append(" default ").append(currentTimestamp).endOfStatement();
    apply.append("alter table ").append(baseTableName).append(" add column ")
      .append(sysPeriodEnd).append(" ").append(sysPeriodType).endOfStatement();

    if (whenCreatedColumn != null) {
      apply.append("update ").append(baseTableName).append(" set ").append(sysPeriodStart).append(" = ").append(whenCreatedColumn).endOfStatement();
    }
  }

  protected void createHistoryTable(DdlBuffer apply, MTable table) throws IOException {

    apply.append("create table ").append(table.getName()).append(historySuffix).append("(").newLine();

    Collection<MColumn> cols = table.allColumns();
    for (MColumn column : cols) {
      if (!column.isDraftOnly()) {
        writeColumnDefinition(apply, column.getName(), column.getType());
        apply.append(",").newLine();
      }
    }
    writeColumnDefinition(apply, sysPeriodStart, sysPeriodType);
    apply.append(",").newLine();
    writeColumnDefinition(apply, sysPeriodEnd, sysPeriodType);
    apply.newLine().append(")").endOfStatement();
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, String columnName, String type) throws IOException {

    String platformType = platformDdl.convert(type, false);
    buffer.append("  ");
    buffer.append(platformDdl.lowerColumnName(columnName), 29);
    buffer.append(platformType);
  }

  protected void createWithHistoryView(DdlBuffer apply, String baseTableName) throws IOException {

    apply
      .append("create view ").append(baseTableName).append(viewSuffix)
      .append(" as select * from ").append(baseTableName)
      .append(" union all select * from ").append(baseTableName).append(historySuffix)
      .endOfStatement().end();
  }


  /**
   * For postgres/h2/mysql we need to drop and recreate the view. Well, we could add columns to the end of the view
   * but otherwise we need to drop and create it.
   */
  protected void recreateHistoryView(DbTriggerUpdate update) throws IOException {

    DdlBuffer buffer = update.dropDependencyBuffer();
    // we need to drop the view early/first before any changes to the tables etc
    buffer.append("drop view if exists ").append(update.getBaseTable()).append(viewSuffix).endOfStatement();

    // recreate the view after all ddl modifications - the view requires ALL columns, also the historyExclude ones.
    createWithHistoryView(update.historyViewBuffer(), update.getBaseTable());
  }

  protected void appendSysPeriodColumns(DdlBuffer apply, String prefix) throws IOException {
    appendColumnName(apply, prefix, sysPeriodStart);
    appendColumnName(apply, prefix, sysPeriodEnd);
  }

  protected void dropHistoryTableEtc(DdlBuffer buffer, String baseTableName) throws IOException {

    buffer.append("drop view ").append(baseTableName).append(viewSuffix).endOfStatement();
    dropSysPeriodColumns(buffer, baseTableName);
    buffer.append("drop table ").append(baseTableName).append(historySuffix).endOfStatement().end();
  }

  protected void dropSysPeriodColumns(DdlBuffer buffer, String baseTableName) throws IOException {
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriodStart).endOfStatement();
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriodEnd).endOfStatement();
  }

  protected void appendInsertIntoHistory(DdlBuffer buffer, String historyTable, List<String> columns) throws IOException {

    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriodStart).append(",").append(sysPeriodEnd).append(",");
    appendColumnNames(buffer, columns, "");
    buffer.append(") values (OLD.").append(sysPeriodStart).append(", ").append(currentTimestamp).append(",");
    appendColumnNames(buffer, columns, "OLD.");
    buffer.append(");").newLine();
  }

  protected void appendColumnNames(DdlBuffer buffer, List<String> columns, String columnPrefix) throws IOException {

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
  protected void appendColumnName(DdlBuffer buffer, String prefix, String columnName) throws IOException {

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
  protected List<String> columnNamesForApply(MTable table) {

    return table.allHistoryColumns(true);
  }

}
