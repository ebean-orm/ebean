package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.util.List;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

/**
 * H2 history support using DB triggers to maintain a history table.
 */
public class H2HistoryDdl extends DbTriggerBasedHistoryDdl {

  private static final String TRIGGER_CLASS = "io.ebean.platform.h2.H2HistoryTrigger";

  H2HistoryDdl() {
    this.sysPeriodType = "timestamp";
    this.now = "now()";
    this.sysPeriodEndValue = "now()";
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) {
    buffer.append("drop trigger ").append(updateTriggerName(baseTable)).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlBuffer buffer, String baseTable, List<String> columnNames) {
    addCreateTrigger(buffer, updateTriggerName(baseTable), baseTable);
  }

  private void addCreateTrigger(DdlBuffer apply, String triggerName, String baseTable) {
    // Note that this does not take into account the historyTable name (excepts _history suffix) and
    // does not take into account excluded columns (all columns included in history)
    apply
      .append("create trigger ").append(triggerName).append(" before update,delete on ").append(quote(baseTable))
      .append(" for each row call \"" + TRIGGER_CLASS + "\";").newLine();
  }

}
