package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlOptions;
import io.ebeaninternal.dbmigration.migration.Column;

public class ClickHouseDdl extends PlatformDdl {

  private static final String LOG_TABLE = "ENGINE = Log()";

  public ClickHouseDdl(DatabasePlatform platform) {
    super(platform);
    this.includeStorageEngine = true;
    this.identitySuffix = "";
    this.columnNotNull = null;
  }

  @Override
  protected String columnDefn(Column column) {
    String defn = super.columnDefn(column);
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      return defn;
    } else {
      return "Nullable(" + defn + ")";
    }
  }

  @Override
  public DdlHandler createDdlHandler(DatabaseBuilder.Settings config) {
    return new ClickHouseDdlHandler(config, this);
  }

  @Override
  protected String convertArrayType(String logicalArrayType) {
    return ClickHouseDbArray.logicalToNative(logicalArrayType);
  }

  /**
   * Add an table storage engine to the create table statement.
   */
  @Override
  public void tableStorageEngine(DdlBuffer apply, String storageEngine) {
    if (storageEngine == null) {
      // default to Log() table but really should all be explicit (need arguments for MergeTree etc)
      storageEngine = LOG_TABLE;
    }
    apply.append(" ").append(storageEngine);
  }

  @Override
  public String alterTableAddForeignKey(DdlOptions options, WriteForeignKey request) {
    return null;
  }

  @Override
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return null;
  }

  @Override
  public String tableInlineForeignKey(WriteForeignKey request) {
    return null;
  }

  @Override
  public String dropIndex(String indexName, String tableName, boolean concurrent) {
    return null;
  }

  @Override
  public String createIndex(WriteCreateIndex create) {
    return null;
  }

  @Override
  public String createCheckConstraint(String ckName, String checkConstraint) {
    return null;
  }

  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    // do nothing
  }

  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    // do nothing
  }

  @Override
  public boolean isInlineComments() {
    return false;
  }
}
