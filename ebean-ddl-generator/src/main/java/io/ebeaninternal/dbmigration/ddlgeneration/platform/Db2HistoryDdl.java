package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.DbConstraintNaming;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlAlterTable;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

/**
 * DB2 History support.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class Db2HistoryDdl implements PlatformHistoryDdl {

  private String systemPeriodStart;
  private String systemPeriodEnd;
  private String transactionId;
  private PlatformDdl platformDdl;
  private DbConstraintNaming constraintNaming;
  private String historySuffix;

  @Override
  public void configure(DatabaseConfig config, PlatformDdl platformDdl) {
    this.systemPeriodStart = config.getAsOfSysPeriod() + "_start";
    this.systemPeriodEnd = config.getAsOfSysPeriod() + "_end";
    this.transactionId = config.getAsOfSysPeriod() + "_txn"; // required for DB2
    this.platformDdl = platformDdl;
    this.constraintNaming = config.getConstraintNaming();
    this.historySuffix = config.getHistoryTableSuffix();
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) {
    String tableName = table.getName();
    String historyTableName = historyTable(tableName);

    // DB2 requires an EXACT copy (same column types with null/non-null, same order)
    addSysPeriodColumns(writer, tableName);
    DdlBuffer tableBuf = writer.applyPostAlter();
    tableBuf.append("create table ").append(historyTableName)
      .append(" as (select * from ").append(tableName).append(") with no data");

    if (table.getTablespaceMeta() != null) {
      String tableSpace = platformDdl.extract(table.getTablespaceMeta().getTablespaceName());
      if (tableSpace != null && !tableSpace.isEmpty()) {
        platformDdl.addTablespace(tableBuf,
          tableSpace,
          platformDdl.extract(table.getTablespaceMeta().getIndexTablespace()),
          platformDdl.extract(table.getTablespaceMeta().getLobTablespace()));
      }
    }
    tableBuf.endOfStatement();
    enableSystemVersioning(writer.applyPostAlter(), tableName);
    platformDdl.alterTable(writer, tableName).setHistoryHandled();

    // drop all: We do not drop columns here, as the whole table will be dropped
    disableSystemVersioning(writer.dropAll(), tableName);
    writer.dropAll().append("drop table ").append(historyTableName).endOfStatement();
  }

  void addSysPeriodColumns(DdlWrite writer, String baseTable) {
    platformDdl.alterTableAddColumn(writer, baseTable, systemPeriodStart, "timestamp(12) not null generated always as row begin", null);
    platformDdl.alterTableAddColumn(writer, baseTable, systemPeriodEnd, "timestamp(12) not null generated always as row end", null);
    platformDdl.alterTableAddColumn(writer, baseTable, transactionId, "timestamp(12) generated always as transaction start id", null);
    platformDdl.alterTable(writer, baseTable).append("add period system_time", null)
      .append("(").append(systemPeriodStart).append(",").append(systemPeriodEnd).append(")");
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    dropHistoryTable(writer, dropHistoryTable.getBaseTable());
  }

  protected void dropHistoryTable(DdlWrite writer, String baseTable) {
    disableSystemVersioning(writer.apply(), baseTable);
    writer.apply().append("alter table ").append(baseTable).append(" drop period system_time").endOfStatement();

    // drop the period & period columns
    platformDdl.alterTableDropColumn(writer, baseTable, systemPeriodStart);
    platformDdl.alterTableDropColumn(writer, baseTable, systemPeriodEnd);
    platformDdl.alterTableDropColumn(writer, baseTable, transactionId);

    // drop the history table
    writer.applyPostAlter().append("drop table ").append(historyTable(baseTable)).endOfStatement();
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {
    MTable table = writer.getTable(addHistoryTable.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("MTable " + addHistoryTable.getBaseTable() + " not found in writer? (required for history DDL)");
    }
    createWithHistory(writer, table);
  }

  @Override
  public boolean alterHistoryTables() {
    return true;
  }

  @Override
  public void updateTriggers(DdlWrite writer, String tableName) {
    DdlAlterTable alter = platformDdl.alterTable(writer, tableName);
    MTable table = writer.getTable(tableName);
    if (table.isWithHistory() && !alter.isHistoryHandled()) {
      disableSystemVersioning(writer.apply(), tableName);
      enableSystemVersioning(writer.applyPostAlter(), tableName);
      alter.setHistoryHandled();
    }
  }

  public void disableSystemVersioning(DdlBuffer apply, String tableName) {
    apply.append("alter table ").append(tableName).append(" drop versioning").endOfStatement();
  }

  public void enableSystemVersioning(DdlBuffer apply, String tableName) {
    apply.append("alter table ").append(tableName).append(" add versioning use history table ").append(historyTable(tableName)).endOfStatement();
  }

  protected String historyTable(String tableName) {
    return constraintNaming.normaliseTable(tableName) + historySuffix;
  }

}
