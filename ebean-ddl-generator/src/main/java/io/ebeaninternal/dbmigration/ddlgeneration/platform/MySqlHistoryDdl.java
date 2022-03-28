package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.util.List;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

/**
 * MySql history support using DB triggers to maintain a history table.
 */
public class MySqlHistoryDdl extends DbTriggerBasedHistoryDdl {

  MySqlHistoryDdl() {
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) {
    buffer.append("drop trigger ").append(updateTriggerName(baseTable)).endOfStatement();
    buffer.append("drop trigger ").append(deleteTriggerName(baseTable)).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlBuffer buffer, String baseTable, List<String> columnNames) {
    buffer.append("lock tables ").append(baseTable).append(" write").endOfStatement();
    addBeforeUpdate(buffer, updateTriggerName(baseTable), baseTable, columnNames);
    addBeforeDelete(buffer, deleteTriggerName(baseTable), baseTable, columnNames);
    buffer.appendStatement("unlock tables");
  }

  private void addBeforeUpdate(DdlBuffer apply, String triggerName, String tableName, List<String> columnNames) {
    apply
      .append("delimiter $$").newLine()
      .append("create trigger ").append(triggerName).append(" before update on ").append(tableName)
      .append(" for each row begin").newLine();
    appendInsertIntoHistory(apply, tableName, columnNames);
    apply
      .append("    set NEW.").append(sysPeriod).append("_start = now(6)").endOfStatement()
      .append("end$$").newLine();
  }

  private void addBeforeDelete(DdlBuffer apply, String triggerName, String tableName, List<String> columnNames) {
    apply
      .append("delimiter $$").newLine()
      .append("create trigger ").append(triggerName).append(" before delete on ").append(tableName)
      .append(" for each row begin").newLine();
    appendInsertIntoHistory(apply, tableName, columnNames);
    apply.append("end$$").newLine();
  }

}
