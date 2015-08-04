package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 *
 */
public class PlatformDdl {

  protected final PlatformHistoryDdl historyDdl;

  protected final PlatformTypeConverter typeConverter;

  protected String foreignKeyRestrict = "";

  public PlatformDdl(DbTypeMap platformTypes, PlatformHistoryDdl historyDdl) {
    this.typeConverter = new PlatformTypeConverter(platformTypes);
    this.historyDdl = historyDdl;
  }

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  public String asIdentityColumn(String columnDefn) {
    return columnDefn;
  }

  /**
   * Return the foreign key on delete on update restrict clause.
   */
  public String getForeignKeyRestrict() {
    return foreignKeyRestrict;
  }

  /**
   * Convert the standard type to the platform specific type.
   */
  public String convert(String type, boolean identity) {
    String platformType = typeConverter.convert(type);
    return identity ? asIdentityColumn(platformType) : platformType;
  }

  /**
   * Add history support to this table using the platform specific mechanism.
   */
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    historyDdl.createWithHistory(writer, table);
  }
}
