package io.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to build IntersectionTable.
 */
public class IntersectionBuilder {

  private final String publishTable;
  private final String draftTable;

  private final List<String> columns = new ArrayList<>();

  IntersectionBuilder(String publishTable, String draftTable) {
    this.publishTable = publishTable;
    this.draftTable = draftTable;
  }

  public void addColumn(String column) {
    columns.add(column);
  }

  public IntersectionTable build() {

    String insertSql = insertSql(publishTable);
    String deleteSql = deleteSql(publishTable);

    String draftInsertSql;
    String draftDeleteSql;
    if (publishTable.equals(draftTable)) {
      draftInsertSql = insertSql;
      draftDeleteSql = deleteSql;
    } else {
      draftInsertSql = insertSql(draftTable);
      draftDeleteSql = deleteSql(draftTable);
    }

    return new IntersectionTable(insertSql, deleteSql, draftInsertSql, draftDeleteSql);
  }

  private String insertSql(String tableName) {

    StringBuilder sb = new StringBuilder();
    sb.append("insert into ").append(tableName).append(" (");

    int count = 0;
    for (String column : columns) {
      if (count++ > 0) {
        sb.append(", ");
      }
      sb.append(column);
    }
    sb.append(") values (");
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append("?");
    }
    sb.append(")");

    return sb.toString();
  }

  private String deleteSql(String tableName) {

    StringBuilder sb = new StringBuilder();
    sb.append("delete from ").append(tableName);
    sb.append(" where ");

    int count = 0;
    for (String column : columns) {
      if (count++ > 0) {
        sb.append(" and ");
      }
      sb.append(column);
      sb.append(" = ?");
    }

    return sb.toString();
  }

}
