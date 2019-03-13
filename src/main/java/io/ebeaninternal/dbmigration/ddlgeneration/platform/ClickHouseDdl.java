package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

import java.io.IOException;

public class ClickHouseDdl extends PlatformDdl {

  public ClickHouseDdl(DatabasePlatform platform) {
    super(platform);
    this.includeStorageEngine = true;
  }

  /**
   * Add an table storage engine to the create table statement.
   */
  @Override
  public void tableStorageEngine(DdlBuffer apply, String storageEngine) throws IOException {
    apply.append(" ").append(storageEngine);
  }

  @Override
  protected void writeColumnNotNull(DdlBuffer buffer) {
    // do nothing
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
