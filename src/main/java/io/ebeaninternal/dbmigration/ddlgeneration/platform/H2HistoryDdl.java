package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.h2.H2HistoryTrigger;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.model.MTable;

import java.io.IOException;

/**
 * H2 history support using DB triggers to maintain a history table.
 */
public class H2HistoryDdl extends DbTriggerBasedHistoryDdl {

  private static final String TRIGGER_CLASS = H2HistoryTrigger.class.getName();

  public H2HistoryDdl() {
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) throws IOException {

    buffer.append("drop trigger ").append(updateTriggerName(baseTable)).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = table.getName();
    DdlBuffer apply = writer.applyHistoryTrigger();

    addCreateTrigger(apply, updateTriggerName(baseTableName), baseTableName);
  }

  @Override
  protected void updateHistoryTriggers(DbTriggerUpdate update) throws IOException {

    recreateHistoryView(update);

    DdlBuffer buffer = update.historyTriggerBuffer();
    dropTriggers(buffer, update.getBaseTable());
    addCreateTrigger(buffer, updateTriggerName(update.getBaseTable()), update.getBaseTable());
  }

  private void addCreateTrigger(DdlBuffer apply, String triggerName, String baseTable) throws IOException {

    // Note that this does not take into account the historyTable name (excepts _history suffix) and
    // does not take into account excluded columns (all columns included in history)
    apply
      .append("create trigger ").append(triggerName).append(" before update,delete on ").append(baseTable)
      .append(" for each row call \"" + TRIGGER_CLASS + "\";").newLine();
  }

}
