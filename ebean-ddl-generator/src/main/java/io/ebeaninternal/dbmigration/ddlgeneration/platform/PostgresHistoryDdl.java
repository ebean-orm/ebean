package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.List;

/**
 * Uses DB triggers to maintain a history table.
 */
public class PostgresHistoryDdl extends DbTriggerBasedHistoryDdl {

  PostgresHistoryDdl() {
    this.now = "current_timestamp";
    this.sysPeriodEndValue = "current_timestamp";
  }

  /**
   * Use Postgres create table like to create the history table.
   */
  @Override
  protected void createHistoryTable(DdlBuffer apply, MTable table) {
    apply.append("create table ").append(historyTableName(table.getName()))
      .append("(like ").append(table.getName()).append(")").endOfStatement();
  }

  /**
   * Use Postgres range type rather than start and end timestamps.
   */
  @Override
  protected void addSysPeriodColumns(DdlWrite writer, String baseTableName, String whenCreatedColumn) {
    platformDdl.alterTableAddColumn(writer, baseTableName, sysPeriod, "tstzrange not null", "tstzrange(" + now + ", null)");
    if (whenCreatedColumn != null) {
      writer.applyPostAlter()
        .append("update ").append(baseTableName).append(" set ")
        .append(sysPeriod).append(" = tstzrange(").append(whenCreatedColumn).append(", null)").endOfStatement();
    }
  }

  @Override
  protected void appendSysPeriodColumns(DdlBuffer apply, String prefix) {
    appendColumnName(apply, prefix, sysPeriod);
  }

  @Override
  protected void dropSysPeriodColumns(DdlWrite writer, String baseTableName) {
    platformDdl.alterTableDropColumn(writer, baseTableName, sysPeriod);
  }

  @Override
  protected void createTriggers(DdlBuffer buffer, String baseTableName, List<String> columnNames) {
    String procedureName = procedureName(baseTableName);
    String triggerName = triggerName(baseTableName);
    createOrReplaceFunction(buffer, procedureName, historyTableName(baseTableName), columnNames);
    buffer
      .append("create trigger ").append(triggerName).newLine()
      .append("  before update or delete on ").append(baseTableName).newLine()
      .append("  for each row execute procedure ").append(procedureName).append("();").newLine().newLine();
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) {
    // rollback trigger then function
    buffer.append("drop trigger if exists ").append(triggerName(baseTable)).append(" on ").append(baseTable).append(" cascade").endOfStatement();
    buffer.append("drop function if exists ").append(procedureName(baseTable)).append("()").endOfStatement();
    buffer.end();
  }

  protected void createOrReplaceFunction(DdlBuffer apply, String procedureName, String historyTable, List<String> includedColumns) {
    apply
      .append("create or replace function ").append(procedureName).append("() returns trigger as $$").newLine();

    apply.append("-- play-ebean-start").newLine();

    apply.append("declare").newLine()
      .append("  lowerTs timestamptz;").newLine()
      .append("  upperTs timestamptz;").newLine();

    apply.append("begin").newLine()
      .append("  lowerTs = lower(OLD.sys_period);").newLine()
      .append("  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);").newLine();

    apply
      .append("  if (TG_OP = 'UPDATE') then").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
      .append("    NEW.").append(sysPeriod).append(" = tstzrange(upperTs,null);").newLine()
      .append("    return new;").newLine();
    apply
      .append("  elsif (TG_OP = 'DELETE') then").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
      .append("    return old;").newLine();
    apply
      .append("  end if;").newLine()
      .append("end;").newLine()
      .append("-- play-ebean-end").newLine()
      .append("$$ LANGUAGE plpgsql;").newLine();

    apply.end();
  }

  @Override
  protected void appendInsertIntoHistory(DdlBuffer buffer, String historyTable, List<String> columns) {
    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriod).append(",");
    appendColumnNames(buffer, columns, "");
    buffer.append(") values (tstzrange(lowerTs,upperTs), ");
    appendColumnNames(buffer, columns, "OLD.");
    buffer.append(");").newLine();
  }

  @Override
  protected String procedureName(String baseTableName) {
    return normalise(baseTableName) + "_history_version";
  }

  @Override
  protected String triggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_upd";
  }

  @Override
  protected String updateTriggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_upd";
  }

  @Override
  protected String deleteTriggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_del";
  }
}
