package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.ArrayList;
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

    this.sysPeriodStart = sysPeriod+"_start";
    this.sysPeriodEnd = sysPeriod+"_end";
  }

  @Override
  public void regenerateHistoryTriggers(DdlWrite writer, HistoryTableUpdate update) throws IOException {

    MTable table = writer.getTable(update.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("MTable "+update.getBaseTable()+" not found in writer? (required for history DDL)");
    }
    regenerateHistoryTriggers(writer, table, update);
  }

  protected abstract void regenerateHistoryTriggers(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException;

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {

    String baseTable = dropHistoryTable.getBaseTable();

    // drop in appropriate order
    dropTriggers(writer.dropHistory(), baseTable);
    dropHistoryTableEtc(writer.dropHistory(), baseTable);
  }


  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {

    String baseTable = addHistoryTable.getBaseTable();
    MTable table = writer.getTable(baseTable);
    if (table == null) {
      throw new IllegalStateException("MTable "+baseTable+" not found in writer? (required for history DDL)");
    }

    createWithHistory(writer, table);
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {

    String baseTable = table.getName();
    String whenCreatedColumn = table.getWhenCreatedColumn();

    // rollback changes in appropriate order
    dropTriggers(writer.rollback(), baseTable);
    dropHistoryTableEtc(writer.rollback(), baseTable);

    addHistoryTable(writer, table, whenCreatedColumn);
    addStoredFunction(writer, table, null);
    createTriggers(writer, table);
  }

  protected abstract void createTriggers(DdlWrite writer, MTable table) throws IOException;

  protected abstract void dropTriggers(DdlBuffer buffer, String baseTable) throws IOException;

  protected void addStoredFunction(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException {
    // do nothing
  }

  protected String normalise(String tableName) {
    return constraintNaming.normaliseTable(tableName);
  }

  protected String historyTableName(String baseTableName) {
    return normalise(baseTableName) + historySuffix;
  }

  protected String procedureName(String baseTableName) {
    return normalise(baseTableName) + "_history_version";
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

    DdlBuffer apply = writer.applyHistory();

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

    Collection<MColumn> cols = table.getColumns().values();
    for (MColumn column : cols) {
      writeColumnDefinition(apply, column.getName(), column.getType());
      apply.append(",").newLine();
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

  protected void dropHistoryTableEtc(DdlBuffer buffer, String baseTableName) throws IOException {

    buffer.append("drop view ").append(baseTableName).append(viewSuffix).endOfStatement();
    dropSysPeriodColumns(buffer, baseTableName);
    buffer.append("drop table ").append(baseTableName).append(historySuffix).endOfStatement().end();
  }

  protected void dropSysPeriodColumns(DdlBuffer buffer, String baseTableName) throws IOException {
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriodStart).endOfStatement();
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriodEnd).endOfStatement();
  }

  //protected abstract void addFunction(DdlBuffer apply, String procedureName, String historyTable, List<String> includedColumns) throws IOException;

  protected void appendInsertIntoHistory(DdlBuffer buffer, String historyTable, List<String> columns) throws IOException {

    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriodStart).append(",").append(sysPeriodEnd).append(",");
    appendColumnNames(buffer, columns, "");
    buffer.append(") values (OLD.").append(sysPeriodStart).append(", ").append(currentTimestamp).append(",");
    appendColumnNames(buffer, columns, "OLD.");
    buffer.append(");").newLine();
  }

  protected void appendColumnNames(DdlBuffer buffer, List<String> columns, String columnPrefix) throws IOException {

    for (int i=0; i< columns.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      buffer.append(columnPrefix);
      buffer.append(columns.get(i));
    }
  }

  /**
   * Return the list of included columns in order.
   */
  protected List<String> includedColumnNames(MTable table) throws IOException {

    Collection<MColumn> columns = table.getColumns().values();
    List<String> includedColumns = new ArrayList<String>(columns.size());

    for (MColumn column : columns) {
      if (!column.isHistoryExclude()) {
        includedColumns.add(column.getName());
      }
    }
    return includedColumns;
  }
}
