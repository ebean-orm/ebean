package io.ebean.platform.h2;

import org.h2.api.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * H2 database trigger used to populate history tables to support the @History feature.
 */
public class H2HistoryTrigger implements Trigger {

  private static final Logger log = LoggerFactory.getLogger(H2HistoryTrigger.class);

  /**
   * Hardcoding the column and history table suffix for now. Not sure how to get that
   * configuration into the trigger instance nicely as it is instantiated by H2.
   */
  private static final String SYS_PERIOD_START = "SYS_PERIOD_START";
  private static final String SYS_PERIOD_END = "SYS_PERIOD_END";
  private static final String HISTORY_SUFFIX = "_history";

  /**
   * SQL to insert into the history table.
   */
  private String insertHistorySql;

  /**
   * Position of SYS_PERIOD_START column in the Object[].
   */
  private int effectStartPosition;

  /**
   * Position of SYS_PERIOD_END column in the Object[].
   */
  private int effectEndPosition;

  @Override
  public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
    // get the columns for the table
    ResultSet rs = conn.getMetaData().getColumns(null, schemaName, tableName, null);

    // build the insert into history table SQL
    StringBuilder insertSql = new StringBuilder(150);
    insertSql.append("insert into ").append(schemaName).append(".").append(tableName).append(HISTORY_SUFFIX).append(" (");

    int count = 0;
    while (rs.next()) {
      if (++count > 1) {
        insertSql.append(",");
      }
      String columnName = rs.getString("COLUMN_NAME");
      if (columnName.equalsIgnoreCase(SYS_PERIOD_START)) {
        this.effectStartPosition = count - 1;
      } else if (columnName.equalsIgnoreCase(SYS_PERIOD_END)) {
        this.effectEndPosition = count - 1;
      }
      insertSql.append(columnName);
    }
    insertSql.append(") values (");
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        insertSql.append(",");
      }
      insertSql.append("?");
    }
    insertSql.append(");");

    this.insertHistorySql = insertSql.toString();
    log.debug("History table insert sql: {}", insertHistorySql);
  }

  @Override
  public void fire(Connection connection, Object[] oldRow, Object[] newRow) throws SQLException {
    if (oldRow != null) {
      // a delete or update event
      LocalDateTime now = LocalDateTime.now();
      oldRow[effectEndPosition] = now;
      if (newRow != null) {
        // update event. Set the effective start timestamp to now.
        newRow[effectStartPosition] = now;
      }
      if (log.isTraceEnabled()) {
        log.trace("History insert: {}", Arrays.toString(oldRow));
      }
      insertIntoHistory(connection, oldRow);
    }
  }

  /**
   * Insert the data into the history table.
   */
  private void insertIntoHistory(Connection connection, Object[] oldRow) throws SQLException {
    try (PreparedStatement stmt = connection.prepareStatement(insertHistorySql)) {
      for (int i = 0; i < oldRow.length; i++) {
        stmt.setObject(i + 1, oldRow[i]);
      }
      stmt.executeUpdate();
    }
  }

  @Override
  public void close() throws SQLException {
    // do nothing
  }

  @Override
  public void remove() throws SQLException {
    // do nothing
  }
}
