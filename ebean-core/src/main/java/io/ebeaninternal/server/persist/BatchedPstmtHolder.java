package io.ebeaninternal.server.persist;

import jakarta.persistence.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Used to hold BatchedPstmt objects for batch based execution.
 * <p>
 * The BatchControl 'front ends' the batching by queuing the persist requests
 * and ordering them according to depth and type. This object should only batch
 * statements of a single 'depth' at any given time.
 * </p>
 */
public final class BatchedPstmtHolder {

  /**
   * A Map of the statements using a String key. This is used so that the same
   * Statement,Prepared,Callable is reused.
   */
  private Map<String, BatchedPstmt> stmtMap = new LinkedHashMap<>();

  /**
   * The Max size across all the BatchedPstmt.
   */
  private int maxSize;

  BatchedPstmtHolder() {
  }

  /**
   * Return the PreparedStatement if it has already been used in this Batch.
   * This will return null if no matching PreparedStatement is found.
   */
  PreparedStatement stmt(String stmtKey, BatchPostExecute postExecute) throws SQLException {
    BatchedPstmt batchedPstmt = batchedPstmt(stmtKey);
    return (batchedPstmt == null) ? null : batchedPstmt.statement(postExecute);
  }

  /**
   * Return the BatchedPstmt that holds the batched statement.
   */
  public BatchedPstmt batchedPstmt(String stmtKey) {
    BatchedPstmt bs = stmtMap.get(stmtKey);
    if (bs == null) {
      return null;
    }
    // maintain a max batch size for any given batched stmt.
    // Used to determine when to flush.
    int bsSize = bs.size();
    if (bsSize > maxSize) {
      maxSize = bsSize;
    }
    return bs;
  }

  /**
   * Return the size of the biggest batched statement.
   * Used to determine when to flush the batch.
   */
  int maxSize() {
    return maxSize;
  }

  /**
   * Add a new PreparedStatement wrapped in the BatchStatement object.
   */
  public void addStmt(BatchedPstmt bs, BatchPostExecute postExecute) {
    bs.add(postExecute);
    stmtMap.put(bs.sql(), bs);
  }

  /**
   * Return true if the batch has no statements to execute.
   */
  public boolean isEmpty() {
    if (stmtMap.isEmpty()) {
      return true;
    }
    for (BatchedPstmt bs : stmtMap.values()) {
      if (!bs.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Execute one of the batched statements returning the row counts.
   */
  public int[] execute(String key, boolean getGeneratedKeys) throws SQLException {
    BatchedPstmt batchedPstmt = stmtMap.get(key);
    if (batchedPstmt == null) {
      throw new PersistenceException("No batched statement found for key " + key);
    }
    batchedPstmt.executeBatch(getGeneratedKeys);
    return batchedPstmt.results();
  }

  /**
   * Execute all batched PreparedStatements.
   */
  public void flush(boolean getGeneratedKeys, boolean reset) throws BatchedSqlException {
    // if there are Listeners/Controllers that interact with the database,
    // the flush may get called recursively in executeBatch/postExecute.
    // which means this needs to process a copy of stmtMap, create a new stmtMap and loadBack after
    final Map<String, BatchedPstmt> copyMap = stmtMap;
    final Collection<BatchedPstmt> copy = copyMap.values();
    this.stmtMap = new LinkedHashMap<>();
    this.maxSize = 0;
    try {
      executeAll(copy, getGeneratedKeys);
      if (reset) {
        closeStatements(copy);
      } else {
        loadBack(copyMap);
      }
    } catch (BatchedSqlException e) {
      copy.forEach(BatchedPstmt::undo);
      closeStatements(copy);
      throw e;
    }
  }

  private void loadBack(Map<String, BatchedPstmt> copyMap) {
    if (stmtMap.isEmpty()) {
      // just restore, was not modified during flush by Listeners/Controllers
      stmtMap = copyMap;
    } else {
      closeStatements(copyMap.values());
    }
  }

  private void executeAll(Collection<BatchedPstmt> values, boolean getGeneratedKeys) throws BatchedSqlException {
    for (BatchedPstmt bs : values) {
      try {
        bs.executeBatch(getGeneratedKeys);
      } catch (SQLException ex) {
        throw new BatchedSqlException("Error when batch flush on: " + bs.identifiers() + ", sql: " + bs.sql(), ex);
      }
    }
  }

  public void clear() {
    closeStatements(stmtMap.values());
    stmtMap.clear();
    maxSize = 0;
  }

  private void closeStatements(Collection<BatchedPstmt> batchedStatements) {
    for (BatchedPstmt bs : batchedStatements) {
      bs.close();
    }
  }
}
