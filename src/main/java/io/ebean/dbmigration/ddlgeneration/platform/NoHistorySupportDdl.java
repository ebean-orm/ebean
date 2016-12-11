package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebean.dbmigration.ddlgeneration.DdlWrite;
import io.ebean.dbmigration.migration.AddHistoryTable;
import io.ebean.dbmigration.migration.DropHistoryTable;
import io.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * Default history implementation that does nothing. Needs to be replaced
 * with an appropriate implementation for the given database platform.
 */
public class NoHistorySupportDdl implements PlatformHistoryDdl {

  @Override
  public void configure(ServerConfig serverConfig, PlatformDdl platformDdl) {
    // does nothing
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    // does nothing
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    // does nothing
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {
    // does nothing
  }

  @Override
  public void updateTriggers(DdlWrite write, HistoryTableUpdate update) {
    // does nothing
  }
}
