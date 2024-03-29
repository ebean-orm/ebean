package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

public class HanaColumnStoreDdl extends AbstractHanaDdl {

  public HanaColumnStoreDdl(DatabasePlatform platform) {
    super(platform);
    this.createTable = "create column table";
  }

  @Override
  public String createIndex(WriteCreateIndex create) {
    final String[] columns = create.getColumns();
    if (columns == null || columns.length == 0) {
      return "-- cannot create index: no columns given";
    }
    if (columns.length == 1) {
      return "-- explicit index \"" + create.getIndexName() + "\" for single column \"" + columns[0] + "\" of table \"" + create.getTableName()
          + "\" is not necessary";
    }

    StringBuilder buffer = new StringBuilder();
    buffer.append("create inverted hash index ").append(maxConstraintName(create.getIndexName())).append(" on ").append(create.getTableName());
    appendColumns(columns, buffer);
    return buffer.toString();
  }

  @Override
  public String dropIndex(String indexName, String tableName, boolean concurrent) {
    DdlBuffer buffer = new BaseDdlBuffer();
    buffer.append("delimiter $$").newLine();
    buffer.append("do").newLine();
    buffer.append("begin").newLine();
    buffer.append("declare exit handler for sql_error_code 261 begin end").endOfStatement();
    buffer.append("exec '").append(dropIndexIfExists).append(maxConstraintName(indexName)).append("'")
        .endOfStatement();
    buffer.append("end").endOfStatement();
    buffer.append("$$");
    return buffer.getBuffer();
  }

}
