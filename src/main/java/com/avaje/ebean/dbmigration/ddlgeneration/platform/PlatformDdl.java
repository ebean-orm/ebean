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

  protected boolean useSequences;

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

  /**
   * Generate and return the create sequence DDL.
   */
  public String createSequence(String sequenceName, int initialValue, int allocationSize) {

    if (!useSequences || sequenceName == null || sequenceName.trim().length() == 0) {
      return null;
    }

    StringBuilder sb = new StringBuilder("create sequence ");
    sb.append(sequenceName);
    if (initialValue > 1) {
      sb.append(" start with ").append(initialValue);
    }
    if (allocationSize > 0 && allocationSize != 50) {
      // at this stage ignoring allocationSize 50 as this is the 'default' and
      // not consistent with the way Ebean batch fetches sequence values
      sb.append(" increment by ").append(allocationSize);
    }
    sb.append(";");
    return sb.toString();
  }

  public String dropSequence(String sequenceName) {
    return "drop sequence "+sequenceName+";";
  }
}
