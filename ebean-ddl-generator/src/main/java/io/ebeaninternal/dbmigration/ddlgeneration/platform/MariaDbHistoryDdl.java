package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

import java.io.IOException;

/**
 * History DDL for MariaDB.
 */
public class MariaDbHistoryDdl implements PlatformHistoryDdl {

  @Override
  public void configure(DatabaseConfig config, PlatformDdl platformDdl) {
    // do nothing
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    String baseTable = table.getName();
    enableSystemVersioning(writer, baseTable);
  }

  private void enableSystemVersioning(DdlWrite writer, String baseTable) throws IOException {
    DdlBuffer apply = writer.applyHistoryView();
    apply.append("alter table ").append(baseTable).append(" add system versioning").endOfStatement();

    DdlBuffer drop = writer.dropAll();
    drop.append("alter table ").append(baseTable).append(" drop system versioning").endOfStatement();
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {
    String baseTable = dropHistoryTable.getBaseTable();
    DdlBuffer apply = writer.applyHistoryView();
    apply.append("alter table ").append(baseTable).append(" drop system versioning").endOfStatement();
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {
    String baseTable = addHistoryTable.getBaseTable();
    enableSystemVersioning(writer, baseTable);
  }

  @Override
  public void updateTriggers(DdlWrite writer, HistoryTableUpdate baseTable) {
    // do nothing
  }
}
