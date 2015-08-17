package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.List;

/**
 * MySql history support using DB triggers to maintain a history table.
 */
public class MySqlHistoryDdl extends DbTriggerBasedHistoryDdl {


  public MySqlHistoryDdl() {
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) throws IOException {

    buffer.append("drop trigger ").append(updateTriggerName(baseTable)).endOfStatement();
    buffer.append("drop trigger ").append(deleteTriggerName(baseTable)).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = table.getName();
    String historyTableName = historyTableName(baseTableName);
    List<String> includedColumns = includedColumnNames(table);

    DdlBuffer apply = writer.applyHistory();

    addBeforeUpdate(apply, updateTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
    addBeforeDelete(apply, deleteTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
  }

  @Override
  protected void regenerateHistoryTriggers(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException {

    String baseTableName = table.getName();
    String historyTableName = historyTableName(baseTableName);
    List<String> includedColumns = includedColumnNames(table);

    DdlBuffer apply = writer.applyHistory();

    apply.append("-- Regenerated ").newLine();
    apply.append("-- changes: ").append(update.description()).newLine();
    // lock the base table while we drop and recreate the triggers
    apply.append("lock tables ").append(baseTableName).append(" write").endOfStatement();
    dropTriggers(apply, baseTableName);
    addBeforeUpdate(apply, updateTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
    addBeforeDelete(apply, deleteTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
    apply.append("unlock tables").endOfStatement();

    // put a reverted version into the rollback buffer
    update.toRevertedColumns(includedColumns);

    DdlBuffer rollback = writer.rollback();
    rollback.append("-- Revert regenerated ").newLine();
    rollback.append("-- revert changes: ").append(update.description()).newLine();
    // lock the base table while we drop and recreate the triggers
    rollback.append("lock tables ").append(baseTableName).append(" write").endOfStatement();
    dropTriggers(rollback, baseTableName);
    addBeforeUpdate(rollback, updateTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
    addBeforeDelete(rollback, deleteTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
    rollback.append("unlock tables").endOfStatement();
  }

  private void addBeforeUpdate(DdlBuffer apply, String triggerName, String baseTable, String historyTable, List<String> includedColumns) throws IOException {

    apply
        .append("delimiter $$").newLine()
        .append("create trigger ").append(triggerName).append(" before update on ").append(baseTable)
        .append(" for each row begin").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
        .append("    set NEW.").append(sysPeriod).append("_start = now(6)").endOfStatement()
        .append("end$$").newLine();
  }

  private void addBeforeDelete(DdlBuffer apply, String triggerName, String baseTable, String historyTable, List<String> includedColumns) throws IOException {

    apply
        .append("delimiter $$").newLine()
        .append("create trigger ").append(triggerName).append(" before delete on ").append(baseTable)
        .append(" for each row begin").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply.append("end$$").newLine();
  }

}
