package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlAlterTable;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

/**
 * History DDL for MariaDB.
 */
public class MariaDbHistoryDdl implements PlatformHistoryDdl {

  private PlatformDdl platformDdl;

  @Override
  public void configure(DatabaseBuilder config, PlatformDdl platformDdl) {
    this.platformDdl = platformDdl;
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) {
    String baseTable = table.getName();
    enableSystemVersioning(writer, baseTable);
  }

  private void enableSystemVersioning(DdlWrite writer, String baseTable) {
    platformDdl.alterTable(writer, baseTable).append("add system versioning", null);

    DdlBuffer drop = writer.dropAll();
    drop.append("alter table ").append(baseTable).append(" drop system versioning").endOfStatement();
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    String baseTable = dropHistoryTable.getBaseTable();
    platformDdl.alterTable(writer, baseTable).append("drop system versioning", null);
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {
    String baseTable = addHistoryTable.getBaseTable();
    enableSystemVersioning(writer, baseTable);
  }

  @Override
  public void updateTriggers(DdlWrite writer, String tableName) {
    MTable table = writer.getTable(tableName);
    // For MariaDB we need to enable system_versioning_alter_history only once
    // per DDL script. This info is stored in the virtual "__$HISTORY_FLAG__" table
    DdlAlterTable history = platformDdl.alterTable(writer, "__$HISTORY_FLAG__");
    if (table != null && table.isWithHistory() && !history.isHistoryHandled()) {
      writer.apply().appendStatement("SET @@system_versioning_alter_history = 1");
      history.setHistoryHandled();
    }
  }
}
