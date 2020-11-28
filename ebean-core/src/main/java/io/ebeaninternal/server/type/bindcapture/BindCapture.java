package io.ebeaninternal.server.type.bindcapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds bind values that can be used to obtain an explain plan.
 */
public class BindCapture {

  private final List<BindCaptureEntry> entries = new ArrayList<>();

  public void add(BindCaptureEntry entry) {
    this.entries.add(entry);
  }

  /**
   * Prepare for explain plan statement execution.
   */
  public void prepare(PreparedStatement explainStmt, Connection connection) throws SQLException {
    for (BindCaptureEntry entry : entries) {
      entry.bind(explainStmt, connection);
    }
  }

  @Override
  public String toString() {
    return entries.toString();
  }

}
