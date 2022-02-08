package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.model.MTable;

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
  protected void createTriggers(DdlWrite writer, MTable table) {

    DbTriggerUpdate update = createDbTriggerUpdate(writer, table);

    addBeforeUpdate(updateTriggerName(update.getBaseTable()), update);
    addBeforeDelete(deleteTriggerName(update.getBaseTable()), update);
  }

  @Override
  protected void updateHistoryTriggers(DbTriggerUpdate update) {

    recreateHistoryView(update);

    DdlBuffer buffer = update.historyTriggerBuffer();
    String baseTable = update.getBaseTable();

    dropTriggers(buffer, baseTable);
    addBeforeUpdate(updateTriggerName(baseTable), update);
    addBeforeDelete(deleteTriggerName(baseTable), update);
  }

  private void addBeforeUpdate(String triggerName, DbTriggerUpdate update) {

    DdlBuffer apply = update.historyTriggerBuffer();
    apply
      .append("delimiter $$").newLine()
      .append("create trigger ").append(triggerName).append(" before update on ").append(update.getBaseTable())
      .append(" for each row begin").newLine();
    appendInsertIntoHistory(apply, update.getHistoryTable(), update.getColumns());
    apply
      .append("    set NEW.").append(sysPeriod).append("_start = now(6)").endOfStatement()
      .append("end$$").newLine();
  }

  private void addBeforeDelete(String triggerName, DbTriggerUpdate update) {

    DdlBuffer apply = update.historyTriggerBuffer();
    apply
      .append("delimiter $$").newLine()
      .append("create trigger ").append(triggerName).append(" before delete on ").append(update.getBaseTable())
      .append(" for each row begin").newLine();
    appendInsertIntoHistory(apply, update.getHistoryTable(), update.getColumns());
    apply.append("end$$").newLine();
  }

}
