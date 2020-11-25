package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A batched statement that is held in BatchedPstmtHolder. It has a list of
 * BatchPostExecute which it will process after the statement is executed.
 * <p>
 * This can hold CallableStatements as well.
 * </p>
 */
public class BatchedPstmt implements SpiProfileTransactionEvent {

  private static final Logger log = LoggerFactory.getLogger(BatchedPstmt.class);

  /**
   * The underlying statement.
   */
  private PreparedStatement pstmt;

  /**
   * True if an insert that uses generated keys.
   */
  private final boolean isGenKeys;

  /**
   * The list of BatchPostExecute used to perform post processing.
   */
  private final List<BatchPostExecute> list = new ArrayList<>();

  private final String sql;

  private final SpiTransaction transaction;

  private long profileStart;
  private long timedStart;

  private int[] results;

  private List<InputStream> inputStreams;

  /**
   * Create with a given statement.
   */
  public BatchedPstmt(PreparedStatement pstmt, boolean isGenKeys, String sql, SpiTransaction transaction) throws SQLException {
    this.pstmt = pstmt;
    this.pstmt.clearBatch();
    this.isGenKeys = isGenKeys;
    this.sql = sql;
    this.transaction = transaction;
  }

  /**
   * Return the number of batched statements.
   */
  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Return the sql
   */
  public String getSql() {
    return sql;
  }

  /**
   * Return the statement adding the postExecute task.
   */
  public PreparedStatement getStatement(BatchPostExecute postExecute) throws SQLException {
    if (postExecute.isFlushQueue() && list.size() >= 20) {
      flushStatementBatch();
    }
    list.add(postExecute);
    return pstmt;
  }

  /**
   * Flush this PreparedStatement using executeBatch() as this was queued element collection
   * or intersection table sql (and otherwise it can be unlimited size).
   */
  private void flushStatementBatch() throws SQLException {
    final int[] rows = pstmt.executeBatch();
    if (rows.length != list.size()) {
      throw new IllegalStateException("Invalid state on executeBatch, rows:" + rows.length + " != " + list.size());
    }
    postExecute();
    list.clear();
  }

  /**
   * Add the BatchPostExecute to the list for post execute processing.
   */
  public void add(BatchPostExecute batchExecute) {
    list.add(batchExecute);
  }

  /**
   * Execute the statement using executeBatch().
   * Run any post processing including getGeneratedKeys.
   */
  public void executeBatch(boolean getGeneratedKeys) throws SQLException {
    if (list.isEmpty()) {
      return;
    }
    timedStart = System.nanoTime();
    profileStart = transaction.profileOffset();
    executeAndCheckRowCounts();
    if (isGenKeys && getGeneratedKeys) {
      getGeneratedKeys();
    }
    postExecute();
    addTimingMetrics();
    list.clear();
    transaction.profileEvent(this);
  }

  private void addTimingMetrics() {
    // just use the first persist request to add batch metrics
    list.get(0).addTimingBatch(timedStart, list.size());
  }

  @Override
  public void profile() {
    // just use the first to add the event
    list.get(0).profile(profileStart, list.size());
  }

  /**
   * Close the underlying statement.
   */
  public void close() {
    if (pstmt != null) {
      try {
        pstmt.close();
      } catch (SQLException e) {
        log.warn("Error closing statement", e);
      } finally {
        pstmt = null;
      }
    }
  }

  private void postExecute() {
    for (BatchPostExecute item : list) {
      item.postExecute();
    }
  }

  private void executeAndCheckRowCounts() throws SQLException {
    try {
      results = pstmt.executeBatch();
      if (results.length != list.size()) {
        throw new SQLException("Invalid state on executeBatch, rows:" + results.length + " != " + list.size());
      }
      // check for concurrency exceptions...
      for (int i = 0; i < results.length; i++) {
        list.get(i).checkRowCount(results[i]);
      }
    } finally {
      closeInputStreams();
    }
  }

  private void getGeneratedKeys() throws SQLException {
    int index = 0;
    try (ResultSet rset = pstmt.getGeneratedKeys()) {
      while (rset.next()) {
        Object idValue = rset.getObject(1);
        list.get(index).setGeneratedKey(idValue);
        index++;
      }
    }
  }

  /**
   * Return the execution results (row counts).
   */
  public int[] getResults() {
    return results;
  }

  /**
   * Register any inputStreams that should be closed after execution.
   */
  public void registerInputStreams(List<InputStream> streams) {
    if (streams != null) {
      if (this.inputStreams == null) {
        this.inputStreams = new ArrayList<>();
      }
      this.inputStreams.addAll(streams);
    }
  }

  private void closeInputStreams() {
    if (inputStreams != null) {
      for (InputStream inputStream : inputStreams) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Error closing inputStream ", e);
        }
      }
      inputStreams = null;
    }
  }
}
