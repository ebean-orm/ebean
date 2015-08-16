package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Uses DB triggers to maintain a history table.
 */
public class PostgresHistoryDdl implements PlatformHistoryDdl {

  private DbConstraintNaming constraintNaming;

  private String sysPeriod;

  private String viewSuffix;

  private String historySuffix;

  public PostgresHistoryDdl() {
  }

  @Override
  public void configure(ServerConfig serverConfig) {
    this.sysPeriod = serverConfig.getAsOfSysPeriod();
    this.viewSuffix = serverConfig.getAsOfViewSuffix();
    this.historySuffix = serverConfig.getHistoryTableSuffix();
    this.constraintNaming = serverConfig.getConstraintNaming();
  }

  @Override
  public void regenerateHistoryTriggers(DdlWrite writer, HistoryTableUpdate update) throws IOException {

    MTable table = writer.getTable(update.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("MTable "+update.getBaseTable()+" not found in writer? (required for history DDL)");
    }
    addStoredFunction(writer, table, update.getComments());
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {

    String baseTable = dropHistoryTable.getBaseTable();

    // drop in appropriate order
    dropTriggersEtc(writer.dropHistory(), baseTable);
    dropHistoryTableEtc(writer.dropHistory(), baseTable);
  }


  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {

    String baseTable = addHistoryTable.getBaseTable();
    MTable table = writer.getTable(baseTable);
    if (table == null) {
      throw new IllegalStateException("MTable "+baseTable+" not found in writer? (required for history DDL)");
    }

    createWithHistory(writer, table);
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {

    String baseTable = table.getName();
    String whenCreatedColumn = table.getWhenCreatedColumn();

    // rollback changes in appropriate order
    dropTriggersEtc(writer.rollback(), baseTable);
    dropHistoryTableEtc(writer.rollback(), baseTable);

    addHistoryTable(writer, table, whenCreatedColumn);
    addStoredFunction(writer, table, null);
    addTrigger(writer, table);
  }

  protected String normalise(String tableName) {
    return constraintNaming.normaliseTable(tableName);
  }

  protected String historyTableName(String baseTableName) {
    return normalise(baseTableName) + historySuffix;
  }

  protected String procedureName(String baseTableName) {
    return normalise(baseTableName) + "_history_version";
  }

  protected String triggerName(String baseTableName) {
    return normalise(baseTableName) + "_history_upd";
  }

  protected void dropTriggersEtc(DdlBuffer buffer, String baseTable) throws IOException {

    // rollback trigger then function
    buffer.append("drop trigger if exists ").append(triggerName(baseTable)).append(" on ").append(baseTable).append(" cascade").endOfStatement();
    buffer.append("drop function if exists ").append(procedureName(baseTable)).append("()").endOfStatement();
    buffer.end();
  }

  protected void addHistoryTable(DdlWrite writer, MTable table, String whenCreatedColumn) throws IOException {

    String baseTableName = table.getName();

    DdlBuffer apply = writer.applyHistory();

    if (whenCreatedColumn == null) {
      // effective history start as at current timestamp
      whenCreatedColumn = "current_timestamp";
    }

    apply
        .append("alter table ").append(baseTableName)
        .append(" add column ").append(sysPeriod).append(" tstzrange not null default tstzrange(").append(whenCreatedColumn).append(", null)")
        .endOfStatement();

    apply
        .append("create table ").append(baseTableName).append(historySuffix)
        .append(" (like ").append(baseTableName).append(")")
        .endOfStatement();

    apply
        .append("create view ").append(baseTableName).append(viewSuffix)
        .append(" as select * from ").append(baseTableName)
        .append(" union all select * from ").append(baseTableName).append(historySuffix)
        .endOfStatement().end();
  }

  protected void dropHistoryTableEtc(DdlBuffer buffer, String baseTableName) throws IOException {

    buffer.append("drop view ").append(baseTableName).append(viewSuffix).endOfStatement();
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriod).endOfStatement();
    buffer.append("drop table ").append(baseTableName).append(historySuffix).endOfStatement().end();
  }

  protected void addTrigger(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = table.getName();
    String procedureName = procedureName(baseTableName);
    String triggerName = triggerName(baseTableName);

    DdlBuffer apply = writer.applyHistory();
    apply
        .append("create trigger ").append(triggerName).newLine()
        .append("  before insert or update or delete on ").append(baseTableName).newLine()
        .append("  for each row execute procedure ").append(procedureName).append("();").newLine().newLine();
  }

  protected void addStoredFunction(DdlWrite writer, MTable table, List<String> comments) throws IOException {

    String procedureName = procedureName(table.getName());

    DdlBuffer apply = writer.applyHistory();

    if (comments != null && !comments.isEmpty()) {
      apply.append("-- Regenerated ").append(procedureName).newLine();
      apply.append("-- changes: ");
      for (int i = 0; i < comments.size(); i++) {
        if (i > 0) {
          apply.append(", ");
        }
        apply.append(comments.get(i));
      }
      apply.newLine();
    }

    apply
        .append("create or replace function ").append(procedureName).append("() returns trigger as $$").newLine()
        .append("begin").newLine();
    apply
        .append("  if (TG_OP = 'INSERT') then").newLine()
        .append("    NEW.").append(sysPeriod).append(" = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
        .append("    return new;").newLine().newLine();
    apply
        .append("  elsif (TG_OP = 'UPDATE') then").newLine();
    appendInsertIntoHistory(apply, table);
    apply
        .append("    NEW.").append(sysPeriod).append(" = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
        .append("    return new;").newLine().newLine();
    apply
        .append("  elsif (TG_OP = 'DELETE') then").newLine();
    appendInsertIntoHistory(apply, table);
    apply
        .append("    return old;").newLine().newLine();

    apply
        .append("  end if;").newLine()
        .append("end;").newLine()
        .append("$$ LANGUAGE plpgsql;").newLine();

    apply.end();
  }

  protected void appendInsertIntoHistory(DdlBuffer buffer, MTable table) throws IOException {

    String historyTable = historyTableName(table.getName());

    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriod).append(",");
    appendColumnNames(buffer, table, "");
    buffer.append(") values (tstzrange(lower(OLD.").append(sysPeriod).append("), CURRENT_TIMESTAMP), ");
    appendColumnNames(buffer, table, "OLD.");
    buffer.append(");").newLine();
  }

  protected void appendColumnNames(DdlBuffer buffer, MTable table, String columnPrefix) throws IOException {

    Collection<MColumn> columns = table.getColumns().values();
    int i = 0;
    for (MColumn column : columns) {
      if (!column.isHistoryExclude()) {
        if (++i > 1) {
          buffer.append(", ");
        }
        buffer.append(columnPrefix);
        buffer.append(column.getName());
      }
    }
  }
}
