package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 *
 */
public class H2HistoryDdl implements PlatformHistoryDdl {

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    // does nothing, not supported yet
  }
}
