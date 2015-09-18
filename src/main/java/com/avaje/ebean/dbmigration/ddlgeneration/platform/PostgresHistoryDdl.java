package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.List;

/**
 * Uses DB triggers to maintain a history table.
 */
public class PostgresHistoryDdl extends DbTriggerBasedHistoryDdl {


  public PostgresHistoryDdl() {
    this.currentTimestamp = "current_timestamp";
  }

  /**
   * Use Postgres create table like to create the history table.
   */
  @Override
  protected void createHistoryTable(DdlBuffer apply, MTable table) throws IOException {

    String baseTable = table.getName();
    apply
        .append("create table ").append(baseTable).append(historySuffix)
        .append("(like ").append(baseTable).append(")").endOfStatement();
  }

  /**
   * Use Postgres range type rather than start and end timestamps.
   */
  @Override
  protected void addSysPeriodColumns(DdlBuffer apply, String baseTableName, String whenCreatedColumn) throws IOException {
    apply
        .append("alter table ").append(baseTableName)
        .append(" add column ").append(sysPeriod).append(" tstzrange not null default tstzrange(").append(currentTimestamp).append(", null)")
        .endOfStatement();

    if (whenCreatedColumn != null) {
      apply.append("update ").append(baseTableName).append(" set ")
          .append(sysPeriod).append(" = tstzrange(").append(whenCreatedColumn).append(", null)").endOfStatement();
    }
  }

  @Override
  protected void dropSysPeriodColumns(DdlBuffer buffer, String baseTableName) throws IOException {
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriod).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = table.getName();
    String procedureName = procedureName(baseTableName);
    String triggerName = triggerName(baseTableName);

    DdlBuffer apply = writer.applyHistory();
    apply
        .append("create trigger ").append(triggerName).newLine()
        .append("  before update or delete on ").append(baseTableName).newLine()
        .append("  for each row execute procedure ").append(procedureName).append("();").newLine().newLine();
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) throws IOException {
    // rollback trigger then function
    buffer.append("drop trigger if exists ").append(triggerName(baseTable)).append(" on ").append(baseTable).append(" cascade").endOfStatement();
    buffer.append("drop function if exists ").append(procedureName(baseTable)).append("()").endOfStatement();
    buffer.end();
  }

  protected void addFunction(DdlBuffer apply, String procedureName, String historyTable, List<String> includedColumns) throws IOException {
    apply
        .append("create or replace function ").append(procedureName).append("() returns trigger as $$").newLine()
        .append("begin").newLine();
    apply
        .append("  if (TG_OP = 'UPDATE') then").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
        .append("    NEW.").append(sysPeriod).append(" = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
        .append("    return new;").newLine().newLine();
    apply
        .append("  elsif (TG_OP = 'DELETE') then").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
        .append("    return old;").newLine().newLine();
    apply
        .append("  end if;").newLine()
        .append("end;").newLine()
        .append("$$ LANGUAGE plpgsql;").newLine();

    apply.end();
  }

  @Override
  protected void regenerateHistoryTriggers(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException {

    // just replace the stored function with 'create or replace'
    addStoredFunction(writer, table, update);
  }

  @Override
  protected void addStoredFunction(DdlWrite writer, MTable table, HistoryTableUpdate update) throws IOException {

    String procedureName = procedureName(table.getName());
    String historyTable = historyTableName(table.getName());

    List<String> includedColumns = includedColumnNames(table);

    DdlBuffer apply = writer.applyHistory();

    if (update != null) {
      apply.append("-- Regenerated ").append(procedureName).newLine();
      apply.append("-- changes: ").append(update.description()).newLine();
    }

    addFunction(apply, procedureName, historyTable, includedColumns);


    if (update != null) {
      // put a reverted version into the rollback buffer
      update.toRevertedColumns(includedColumns);

      DdlBuffer rollback = writer.rollback();
      rollback.append("-- Revert regenerated ").append(procedureName).newLine();
      rollback.append("-- revert changes: ").append(update.description()).newLine();

      addFunction(rollback, procedureName, historyTable, includedColumns);
    }
  }

  @Override
  protected void appendInsertIntoHistory(DdlBuffer buffer, String historyTable, List<String> columns) throws IOException {

    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriod).append(",");
    appendColumnNames(buffer, columns, "");
    buffer.append(") values (tstzrange(lower(OLD.").append(sysPeriod).append("), current_timestamp), ");
    appendColumnNames(buffer, columns, "OLD.");
    buffer.append(");").newLine();
  }

}
