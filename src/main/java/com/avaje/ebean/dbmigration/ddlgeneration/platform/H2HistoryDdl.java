package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.H2HistoryTrigger;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.List;

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
    String historyTableName = historyTableName(baseTableName);
    List<String> includedColumns = includedColumnNames(table);

    DdlBuffer apply = writer.applyHistory();

    addCreateTrigger(apply, updateTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
  }

  @Override
  protected void regenerateHistoryTriggers(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException {

    String baseTableName = table.getName();
    String historyTableName = historyTableName(baseTableName);
    List<String> includedColumns = includedColumnNames(table);

    DdlBuffer apply = writer.applyHistory();

    apply.append("-- Regenerated ").newLine();
    apply.append("-- changes: ").append(update.description()).newLine();

    dropTriggers(apply, baseTableName);
    addCreateTrigger(apply, updateTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);

    // put a reverted version into the rollback buffer
    update.toRevertedColumns(includedColumns);

    DdlBuffer rollback = writer.rollback();
    rollback.append("-- Revert regenerated ").newLine();
    rollback.append("-- revert changes: ").append(update.description()).newLine();
    dropTriggers(rollback, baseTableName);
    addCreateTrigger(rollback, updateTriggerName(baseTableName), baseTableName, historyTableName, includedColumns);
  }

  private void addCreateTrigger(DdlBuffer apply, String triggerName, String baseTable, String historyTable, List<String> includedColumns) throws IOException {

    // Note that this does not take into account the historyTable name (excepts _history suffix) and
    // does not take into account excluded columns (all columns included in history)
    apply
        .append("create trigger ").append(triggerName).append(" before update,delete on ").append(baseTable)
        .append(" for each row call \"" + TRIGGER_CLASS + "\";").newLine();
  }

}
