package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * Default history implementation that does nothing. Needs to be replaced
 * with an appropriate implementation for the given database platform.
 */
public class NoHistorySupportDdl implements PlatformHistoryDdl {

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {

    // does nothing
  }
}
