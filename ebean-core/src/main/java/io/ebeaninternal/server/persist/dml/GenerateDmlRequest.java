package io.ebeaninternal.server.persist.dml;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to support the generation of DML statements.
 */
public final class GenerateDmlRequest {

  private final StringBuilder sb = new StringBuilder(100);
  private final List<String> columns = new ArrayList<>();
  private StringBuilder insertBindBuffer;
  private String prefix;
  private String prefix2;
  private int insertMode;
  private int bindColumnCount;
  private boolean hasColumnDefn;

  GenerateDmlRequest append(String s) {
    sb.append(s);
    return this;
  }

  /**
   * Append column and type for Insert (ClickHouse).
   */
  public void appendColumnDefn(String columnName, String columnDefn) {
    if (hasColumnDefn) {
      sb.append(", ");
    } else {
      hasColumnDefn = true;
    }
    sb.append(columnName).append(' ').append(columnDefn);
  }

  public void appendColumn(String column) {
    appendColumn(column, "?");
  }

  public void appendColumn(String column, String bind) {
    ++bindColumnCount;
    sb.append(prefix);
    sb.append(column);
    columns.add(column);
    if (insertMode > 0) {
      if (insertMode++ > 1) {
        insertBindBuffer.append(',');
      }
      insertBindBuffer.append(bind);
    } else {
      sb.append('=');
      sb.append(bind);
    }
    if (prefix2 != null) {
      prefix = prefix2;
      prefix2 = null;
    }
  }

  int bindColumnCount() {
    return bindColumnCount;
  }

  String insertBindBuffer() {
    return insertBindBuffer.toString();
  }

  @Override
  public String toString() {
    return sb.toString();
  }

  void setWhereIdMode() {
    this.prefix = "";
    this.prefix2 = " and ";
  }

  void setInsertSetMode() {
    this.insertBindBuffer = new StringBuilder(100);
    this.insertMode = 1;
    this.prefix = "";
    this.prefix2 = ", ";
  }

  void setUpdateSetMode() {
    this.prefix = "";
    this.prefix2 = ", ";
  }

  public boolean isUpdate() {
    return insertMode == 0;
  }

  public List<String> columns() {
    return columns;
  }
}
