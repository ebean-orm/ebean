package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * Defines the implementation for adding history support to a table.
 */
public interface PlatformHistoryDdl {

  /**
   * Add history support to the table using platform specific mechanism.
   */
  void createWithHistory(DdlWrite writer, MTable table) throws IOException;
}
