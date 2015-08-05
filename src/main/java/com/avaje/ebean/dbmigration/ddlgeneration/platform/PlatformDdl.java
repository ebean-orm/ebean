package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.dbmigration.ddlgeneration.BaseDdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import com.avaje.ebean.dbmigration.migration.IdentityType;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 *
 */
public class PlatformDdl {

  protected PlatformHistoryDdl historyDdl = new NoHistorySupportDdl();

  protected DdlNamingConvention namingConvention = new DdlNamingConvention();

  private final PlatformTypeConverter typeConverter;

  private final DbIdentity dbIdentity;

  /**
   * Default assumes if exists is supported.
   */
  protected String dropTableIfExists = "drop table if exists ";

  /**
   * Default assumes if exists is supported.
   */
  protected String dropSequenceIfExists = "drop sequence if exists ";

  protected String foreignKeyRestrict = "on delete restrict on update restrict";

  protected String identitySuffix = " auto_increment";


  public PlatformDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    this.dbIdentity = dbIdentity;
    this.typeConverter = new PlatformTypeConverter(platformTypes);
  }

  public DdlHandler createDdlHandler() {
    return new BaseDdlHandler(namingConvention, this);
  }

  public IdType useIdentityType(IdentityType modelIdentityType) {

    return dbIdentity.useIdentityType(modelIdentityType);
  }

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  public String asIdentityColumn(String columnDefn) {
    return columnDefn + identitySuffix;
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
    return dropSequenceIfExists + sequenceName;
  }

  public String dropTable(String tableName) {
    return dropTableIfExists + tableName;
  }

  public String lowerName(String name) {
    return namingConvention.lowerName(name);
  }

}
