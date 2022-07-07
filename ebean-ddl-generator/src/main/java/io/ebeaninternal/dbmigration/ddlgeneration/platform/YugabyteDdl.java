package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.migration.Column;

import java.util.List;

public final class YugabyteDdl extends PostgresDdl {

  public YugabyteDdl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new YugabyteHistoryDdl();
  }

  @Override
  protected List<Column> sortColumns(List<Column> columns) {
    return columns;
  }
}
