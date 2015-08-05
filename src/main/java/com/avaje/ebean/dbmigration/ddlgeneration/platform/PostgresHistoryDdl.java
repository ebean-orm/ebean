package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;
import java.util.Collection;

/**
 *
 */
public class PostgresHistoryDdl implements PlatformHistoryDdl {

  private final DdlNameNormalise normalise;

  public PostgresHistoryDdl(DdlNameNormalise normalise) {
    this.normalise = normalise;
  }

  protected String historyTableName(String baseTableName) {
    return baseTableName + "_history";
  }

  protected String procedureName(String baseTableName) {
    return baseTableName + "_history_version";
  }

  protected String triggerName(String baseTableName) {
    return baseTableName + "_history_upd";
  }

  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {

    // naming convention

    addHistoryTable(writer, table);
    addStoredFunction(writer, table);
    addTrigger(writer, table);
  }

  public void addHistoryTable(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = this.normalise.normaliseTable(table.getName());

    DdlBuffer buffer = writer.applyHistory();

    buffer
        .append("alter table ").append(baseTableName)
        .append(" add column sys_period tstzrange not null")
        .endOfStatement().end();

    buffer
        .append("create table ").append(baseTableName).append("_history")
        .append(" (like ").append(baseTableName).append(")")
        .endOfStatement().end();

    buffer
        .append("create view ").append(baseTableName).append("_with_history")
        .append(" as select * from").append(baseTableName)
        .append(" union all select * from").append(baseTableName).append("_history")
        .endOfStatement().end();

  }

  public void addTrigger(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = this.normalise.normaliseTable(table.getName());
    String procedureName = procedureName(baseTableName);
    String triggerName = triggerName(baseTableName);

    DdlBuffer buffer = writer.applyHistory();
    buffer
        .append("create trigger ").append(triggerName).newLine()
        .append("  before insert or update or delete on ").append(baseTableName).newLine()
        .append("  for each row execute procedure ").append(procedureName).append("();").newLine().newLine();

  }


  public void addStoredFunction(DdlWrite writer, MTable table) throws IOException {

    String baseTableName = this.normalise.normaliseTable(table.getName());
    String procedureName = procedureName(baseTableName);
    DdlBuffer buffer = writer.applyHistory();

    buffer
        .append("create or replace function ").append(procedureName).append("() returns trigger as $$").newLine()
        .append("begin").newLine();
    buffer
        .append("  if (TG_OP = 'INSERT') then").newLine()
        .append("    NEW.sys_period = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
        .append("    return new;").newLine().newLine();
    buffer
        .append("  elsif (TG_OP = 'UPDATE') then").newLine();
    appendInsertIntoHistory(buffer, table);
    buffer
        .append("    NEW.sys_period = tstzrange(CURRENT_TIMESTAMP,null);").newLine()
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

    buffer.append("    insert into ").append(historyTable).append(" (sys_period,");
    appendColumnNames(buffer, table, "");
    buffer.append(") values (tstzrange(lower(OLD.sys_period), CURRENT_TIMESTAMP), ");
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
