package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.migration.Column;

public class YugabyteDdl extends PostgresDdl {

  public YugabyteDdl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new YugabyteHistoryDdl();
  }

  @Override
  public void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column, boolean onHistoryTable, String defaultValue) {
    super.alterTableAddColumn(buffer, tableName, column, onHistoryTable, defaultValue);

    if (Boolean.TRUE.equals(column.isNotnull()) && defaultValue == null) {
      // this seems to be a bug in yugabyte. Adding notNull column with default value leaves value null.
      buffer.append("update ").append(tableName).append(" set ").append(column.getName()).append(" = ").append(defaultValue).endOfStatement();
    }
  }

}
