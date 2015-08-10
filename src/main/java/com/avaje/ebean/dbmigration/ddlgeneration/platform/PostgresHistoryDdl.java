package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.Collection;

/**
 * Uses DB triggers to maintain a history table.
 */
public class PostgresHistoryDdl implements PlatformHistoryDdl {

  private DbConstraintNaming constraintNaming;

  private String sysPeriod;

  private String viewSuffix;

  private String historySuffix = "_history";

  public PostgresHistoryDdl() {
  }

  @Override
  public void configure(ServerConfig serverConfig) {
    this.sysPeriod = serverConfig.getAsOfSysPeriod();
    this.viewSuffix = serverConfig.getAsOfViewSuffix();
    this.constraintNaming = serverConfig.getConstraintNaming();
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {

    addHistoryTable(writer, table);
    addStoredFunction(writer, table);
    addTrigger(writer, table);
  }

  protected String historyTableName(String baseTableName) {
    return baseTableName + historySuffix;
  }

  protected String procedureName(String baseTableName) {
    return baseTableName + "_history_version";
  }

  protected String triggerName(String baseTableName) {
    return baseTableName + "_history_upd";
  }

  public void addHistoryTable(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = constraintNaming.normaliseTable(table.getName());

    DdlBuffer buffer = writer.applyHistory();

    buffer
        .append("alter table ").append(baseTableName)
        .append(" add column ").append(sysPeriod).append(" tstzrange not null")
        .endOfStatement().end();

    buffer
        .append("create table ").append(baseTableName).append(historySuffix)
        .append(" (like ").append(baseTableName).append(")")
        .endOfStatement().end();

    buffer
        .append("create view ").append(baseTableName).append(viewSuffix)
        .append(" as select * from").append(baseTableName)
        .append(" union all select * from").append(baseTableName).append(historySuffix)
        .endOfStatement().end();

  }

  public void addTrigger(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = constraintNaming.normaliseTable(table.getName());
    String procedureName = procedureName(baseTableName);
    String triggerName = triggerName(baseTableName);

    DdlBuffer buffer = writer.applyHistory();
    buffer
        .append("create trigger ").append(triggerName).newLine()
        .append("  before insert or update or delete on ").append(baseTableName).newLine()
        .append("  for each row execute procedure ").append(procedureName).append("();").newLine().newLine();

  }


  public void addStoredFunction(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = constraintNaming.normaliseTable(table.getName());
    String procedureName = procedureName(baseTableName);
    DdlBuffer buffer = writer.applyHistory();

    buffer
        .append("create or replace function ").append(procedureName).append("() returns trigger as $$").newLine()
        .append("begin").newLine();
    buffer
        .append("  if (TG_OP = 'INSERT') then").newLine()
        .append("    NEW.").append(sysPeriod).append(" = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
        .append("    return new;").newLine().newLine();
    buffer
        .append("  elsif (TG_OP = 'UPDATE') then").newLine();
    appendInsertIntoHistory(buffer, table);
    buffer
        .append("    NEW.").append(sysPeriod).append(" = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
        .append("    return new;").newLine().newLine();
    buffer
        .append("  elsif (TG_OP = 'DELETE') then").newLine();
    appendInsertIntoHistory(buffer, table);
    buffer
        .append("    return old;").newLine().newLine();

    buffer
        .append("  end if;").newLine()
        .append("end;").newLine()
        .append("$$ LANGUAGE plpgsql;").newLine();

    buffer.end();
  }

  protected void appendInsertIntoHistory(DdlBuffer buffer, MTable table) throws IOException {

    String historyTable  = historyTableName(table.getName());

    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriod).append(",");
    appendColumnNames(buffer, table, "");
    buffer.append(") values (tstzrange(lower(OLD.").append(sysPeriod).append("), CURRENT_TIMESTAMP), ");
    appendColumnNames(buffer, table, "OLD.");
    buffer.append(");").newLine();
  }

  protected void appendColumnNames(DdlBuffer buffer, MTable table, String columnPrefix) throws IOException {

    //id, line1, line2, city, country_code, version, when_created, when_updated
    Collection<MColumn> columns = table.getColumns().values();
    int i = 0;
    for (MColumn column : columns) {
      if (++i > 1) {
        buffer.append(", ");
      }
      buffer.append(columnPrefix);
      buffer.append(column.getName());
    }
  }
}
