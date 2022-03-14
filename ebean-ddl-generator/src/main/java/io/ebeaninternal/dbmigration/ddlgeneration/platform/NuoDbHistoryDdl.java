package io.ebeaninternal.dbmigration.ddlgeneration.platform;


import java.util.List;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

/**
 * NuoDB history support using DB triggers to maintain a history table.
 */
public class NuoDbHistoryDdl extends DbTriggerBasedHistoryDdl {

  NuoDbHistoryDdl() {
    this.now = "now()";
    this.sysPeriodEndValue = "NEW.sys_period_start";
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) {

    buffer.append("drop trigger ").append(updateTriggerName(baseTable)).endOfStatement();
    buffer.append("drop trigger ").append(deleteTriggerName(baseTable)).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlBuffer buffer, String baseTable, List<String> columnNames) {

    addBeforeUpdate(buffer, updateTriggerName(baseTable), baseTable, columnNames);
    addBeforeDelete(buffer, deleteTriggerName(baseTable), baseTable, columnNames);
  }

  private void addBeforeUpdate(DdlBuffer apply, String triggerName, String tableName, List<String> columnNames) {

    addTriggerStart(triggerName, tableName, apply, " before update for each row as ");

    apply.append("    NEW.sys_period_start = greatest(current_timestamp, date_add(OLD.sys_period_start, interval 1 microsecond))").endOfStatement();
    appendInsertIntoHistory(apply, tableName, columnNames);
    addEndTrigger(apply);
  }

  private void addBeforeDelete(DdlBuffer apply, String triggerName, String tableName, List<String> columnNames) {

    addTriggerStart(triggerName, tableName, apply, " before delete for each row as");
    appendInsertIntoHistory(apply, tableName, columnNames);
    addEndTrigger(apply);
  }

  private void addTriggerStart(String triggerName, String baseTable, DdlBuffer apply, String s) {
    apply
      .append("delimiter $$").newLine()
      .append("create or replace trigger ").append(triggerName).append(" for ").append(baseTable)
      .append(s).newLine();
  }

  private void addEndTrigger(DdlBuffer apply) {
    apply.append("end_trigger")
      .endOfStatement()
      .append("$$").newLine()
      .newLine();
  }

}
