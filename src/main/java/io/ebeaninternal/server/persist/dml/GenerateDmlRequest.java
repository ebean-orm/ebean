package io.ebeaninternal.server.persist.dml;

/**
 * Helper to support the generation of DML statements.
 */
public class GenerateDmlRequest {

  private static final String IS_NULL = " is null";

  private final StringBuilder sb = new StringBuilder(100);

  private StringBuilder insertBindBuffer;

  private String prefix;
  private String prefix2;

  private int insertMode;

  private int bindColumnCount;

  /**
   * Create from a PersistRequestBean.
   */
  public GenerateDmlRequest() {
  }

  public GenerateDmlRequest append(String s) {
    sb.append(s);
    return this;
  }

  public void appendColumn(String column) {
    //String bind = (insertMode > 0) ? "?" : "=?";
    appendColumn(column, "?");
  }

  public void appendColumn(String column, String bind) {

    ++bindColumnCount;

    sb.append(prefix);
    sb.append(column);
    //sb.append(expr);
    if (insertMode > 0) {
      if (insertMode++ > 1) {
        insertBindBuffer.append(",");
      }
      insertBindBuffer.append(bind);
    } else {
      sb.append("=");
      sb.append(bind);
    }

    if (prefix2 != null) {
      prefix = prefix2;
      prefix2 = null;
    }
  }

  public int getBindColumnCount() {
    return bindColumnCount;
  }

  public String getInsertBindBuffer() {
    return insertBindBuffer.toString();
  }

  @Override
  public String toString() {
    return sb.toString();
  }

  public void setWhereIdMode() {
    this.prefix = "";
    this.prefix2 = " and ";
  }

  public void setInsertSetMode() {
    this.insertBindBuffer = new StringBuilder(100);
    this.insertMode = 1;
    this.prefix = "";
    this.prefix2 = ", ";
  }

  public void setUpdateSetMode() {
    this.prefix = "";
    this.prefix2 = ", ";
  }

  public boolean isUpdate() {
    return insertMode == 0;
  }
}
