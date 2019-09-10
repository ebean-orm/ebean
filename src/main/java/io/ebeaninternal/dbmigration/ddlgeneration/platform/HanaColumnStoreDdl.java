package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

import java.io.IOException;

public class HanaColumnStoreDdl extends AbstractHanaDdl {

  public HanaColumnStoreDdl(DatabasePlatform platform) {
    super(platform);
    this.createTable = "create column table";
  }

  @Override
  public String createIndex(String indexName, String tableName, String[] columns) {
    if (columns == null || columns.length == 0) {
      return "-- cannot create index: no columns given";
    }

    if (columns.length == 1) {
      return "-- explicit index \"" + indexName + "\" for single column \"" + columns[0] + "\" of table \"" + tableName
          + "\" is not necessary";
    }

    StringBuilder buffer = new StringBuilder();
    buffer.append("create inverted hash index ").append(maxConstraintName(indexName)).append(" on ").append(tableName);
    appendColumns(columns, buffer);

    return buffer.toString();
  }

  @Override
  public String dropIndex(String indexName, String tableName) {
    DdlBuffer buffer = new BaseDdlBuffer(null);
    try {
      buffer.append("delimiter $$").newLine();
      buffer.append("do").newLine();
      buffer.append("begin").newLine();
      buffer.append("declare exit handler for sql_error_code 261 begin end").endOfStatement();
      buffer.append("exec '").append(dropIndexIfExists).append(maxConstraintName(indexName)).append("'")
          .endOfStatement();
      buffer.append("end").endOfStatement();
      buffer.append("$$");
      return buffer.getBuffer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
