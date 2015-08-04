package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.ColumnDdl;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.DropColumn;

import java.io.IOException;
import java.util.List;

/**
 */
public class BaseColumnDdl implements ColumnDdl {


  @Override
  public void generate(DdlWrite writer, AddColumn addColumn) throws IOException {

    String tableName = addColumn.getTableName();
    List<Column> columns = addColumn.getColumn();
    for (Column column : columns) {
      // apply
      alterTableAddColumn(writer.apply(), tableName, column);

      // rollback
      alterTableDropColumn(writer.rollback(), tableName, column.getName());
    }
  }

  @Override
  public void generate(DdlWrite writer, DropColumn dropColumn) throws IOException {

    String tableName = dropColumn.getTableName();

    alterTableDropColumn(writer.apply(), tableName, dropColumn.getColumnName());

    // no good rollback option here, it is best if drop columns
    // are put into a separate changeSet that is run last
  }

  protected void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) throws IOException {

    buffer.append("alter table ").append(tableName)
        .append(" drop column ").append(columnName)
        .endOfStatement().end();
  }

  protected void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column) throws IOException {

    buffer.append("alter table ").append(tableName)
        .append(" add column ").append(column.getName())
        .append(" ").append(column.getType());

    if (Boolean.TRUE.equals(column.isNotnull())) {
      buffer.append(" not null");
    }
    if (hasValue(column.getCheckConstraint())) {
      buffer.append(" ").append(column.getCheckConstraint());
    }
    buffer.endOfStatement().end();
  }


  protected boolean hasValue(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
