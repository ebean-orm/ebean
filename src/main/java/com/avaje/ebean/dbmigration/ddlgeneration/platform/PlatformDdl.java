package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.dbmigration.ddlgeneration.BaseDdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.IdentityType;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * Controls the DDL generation for a specific database platform.
 */
public class PlatformDdl {

  protected PlatformHistoryDdl historyDdl = new NoHistorySupportDdl();

  /**
   * Converter for logical/standard types to platform specific types. (eg. clob -> text)
   */
  private final PlatformTypeConverter typeConverter;

  /**
   * For handling support of sequences and autoincrement.
   */
  private final DbIdentity dbIdentity;

  /**
   * Default assumes if exists is supported.
   */
  protected String dropTableIfExists = "drop table if exists ";

  protected String dropTableCascade = "";

  /**
   * Default assumes if exists is supported.
   */
  protected String dropSequenceIfExists = "drop sequence if exists ";

  protected String foreignKeyRestrict = "on delete restrict on update restrict";

  protected String identitySuffix = " auto_increment";

  protected String dropConstraintIfExists = "drop constraint if exists";

  protected String dropIndexIfExists = "drop index if exists ";


  protected String alterColumn =  "alter column";

  protected String columnSetType = "";

  protected String columnSetDefault = "set default";

  protected String columnDropDefault = "drop default";

  protected String columnSetNotnull = "set not null";

  protected String columnSetNull = "set null";

  /**
   * Set false for MsSqlServer to allow multiple nulls for OneToOne mapping.
   */
  protected boolean inlineUniqueOneToOne = true;

  public PlatformDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    this.dbIdentity = dbIdentity;
    this.typeConverter = new PlatformTypeConverter(platformTypes);
  }

  public DdlHandler createDdlHandler(ServerConfig serverConfig) {
    historyDdl.configure(serverConfig);
    return new BaseDdlHandler(serverConfig.getNamingConvention(), serverConfig.getConstraintNaming(), this);
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
   * Return the drop foreign key clause.
   */
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + tableName + " " + dropConstraintIfExists + " " + fkName;
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

  /**
   * Return the drop sequence statement (potentially with if exists clause).
   */
  public String dropSequence(String sequenceName) {
    return dropSequenceIfExists + sequenceName;
  }

  /**
   * Return the drop table statement (potentially with if exists clause).
   */
  public String dropTable(String tableName) {
    return dropTableIfExists + tableName + dropTableCascade;
  }

  /**
   * Return the drop index statement.
   */
  public String dropIndex(String indexName, String tableName) {
    return dropIndexIfExists + indexName;
  }

  /**
   * Return true if unique constraints for OneToOne can be inlined as normal.
   * Returns false for MsSqlServer due to it's null handling for unique constraints.
   */
  public boolean isInlineUniqueOneToOne() {
    return inlineUniqueOneToOne;
  }

  /**
   * Overridden by MsSqlServer for specific null handling on unique constraints.
   */
  public String createExternalUniqueForOneToOne(String uqName, String tableName, String[] columns) {
    // does nothing by default, really this is a MsSqlServer specific requirement
    return "";
  }

  public void historyExcludeColumn(DdlWrite writer, AlterColumn alterColumn) {

  }

  public void historyIncludeColumn(DdlWrite writer, AlterColumn alterColumn) {

  }


  public String alterColumnType(String tableName, String columnName, String type) {

    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + columnSetType + type;
  }

  public String alterColumnNotnull(String tableName, String columnName, boolean notnull) {

    String suffix = notnull ? columnSetNotnull : columnSetNull;
    return  "alter table " + tableName + " " + alterColumn + " " + columnName + " " + suffix;
  }

  public boolean isDropDefault(String defaultValue) {
    return "DROP DEFAULT".equals(defaultValue);
  }

  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {

    String suffix = isDropDefault(defaultValue) ? columnDropDefault : columnSetDefault + " " + defaultValue;
    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + suffix;
  }

  public String alterColumnBaseAttributes(AlterColumn alter) {
    // by default do nothing, only used by mysql as it can only modify the column with the
    // full column definition
    return null;
  }
}
