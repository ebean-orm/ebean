package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebean.dbmigration.ddlgeneration.DdlBuffer;
import io.ebean.dbmigration.ddlgeneration.DdlWrite;
import io.ebean.dbmigration.migration.AddHistoryTable;
import io.ebean.dbmigration.migration.DropHistoryTable;
import io.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServerHistoryDdl implements PlatformHistoryDdl {

  private String systemPeriodStart;
  private String systemPeriodEnd;

  @Override
  public void configure(ServerConfig serverConfig, PlatformDdl platformDdl) {
    this.systemPeriodStart = serverConfig.getAsOfSysPeriod() + "From";
    this.systemPeriodEnd = serverConfig.getAsOfSysPeriod() + "To";
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    String baseTable = table.getName();
    enableSystemVersioning(writer, baseTable);
  }

  private void enableSystemVersioning(DdlWrite writer, String baseTable) throws IOException {
    DdlBuffer apply = writer.applyHistory();
    apply.append("alter table ").append(baseTable).newLine()
      .append("    add ").append(systemPeriodStart).append(" datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),").newLine()
      .append("        ").append(systemPeriodEnd).append("   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL,").newLine()
      .append("period for system_time (").append(systemPeriodStart).append("From, ").append(systemPeriodEnd).append("To)").endOfStatement();

    apply.append("alter table ").append(baseTable).append("set (system_versioning = on)").endOfStatement();
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {
    String baseTable = dropHistoryTable.getBaseTable();
    DdlBuffer apply = writer.applyHistory();
    apply.append("alter table ").append(baseTable).append("set (system_versioning = off)").endOfStatement();
    apply.append("alter table ").append(baseTable).append(" drop column ").append(systemPeriodStart).endOfStatement();
    apply.append("alter table ").append(baseTable).append(" drop column ").append(systemPeriodEnd).endOfStatement();
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {
    String baseTable = addHistoryTable.getBaseTable();
    enableSystemVersioning(writer, baseTable);
  }

  @Override
  public void updateTriggers(DdlWrite write, HistoryTableUpdate baseTable) throws IOException {
    // SQL Server 2016 does not need triggers
  }
}
