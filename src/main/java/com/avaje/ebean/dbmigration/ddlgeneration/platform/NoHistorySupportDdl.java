package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.model.MTable;

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
  public void regenerateHistoryTriggers(DdlWrite write, HistoryTableUpdate update) {
    // does nothing
  }
}
