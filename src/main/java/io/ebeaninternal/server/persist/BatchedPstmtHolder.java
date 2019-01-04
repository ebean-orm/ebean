package io.ebeaninternal.server.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * Used to hold BatchedPstmt objects for batch based execution.
 * <p>
 * The BatchControl 'front ends' the batching by queuing the persist requests
 * and ordering them according to depth and type. This object should only batch
 * statements of a single 'depth' at any given time.
 * </p>
 */
public class BatchedPstmtHolder {

  private static final Logger logger = LoggerFactory.getLogger(BatchedPstmtHolder.class);

  /**
   * A Map of the statements using a String key. This is used so that the same
   * Statement,Prepared,Callable is reused.
   */
  private final LinkedHashMap<String, BatchedPstmt> stmtMap = new LinkedHashMap<>();

  /**
   * The Max size across all the BatchedPstmt.
   */
  private int maxSize;

  public BatchedPstmtHolder() {
  }

  /**
   * Return the PreparedStatement if it has already been used in this Batch.
   * This will return null if no matching PreparedStatement is found.
   */
  public PreparedStatement getStmt(String stmtKey, BatchPostExecute postExecute) {
    BatchedPstmt batchedPstmt = getBatchedPstmt(stmtKey, postExecute);
    return (batchedPstmt == null) ? null : batchedPstmt.getStatement();
  }

  /**
   * Return the BatchedPstmt that holds the batched statement.
   */
  public BatchedPstmt getBatchedPstmt(String stmtKey, BatchPostExecute postExecute) {

    BatchedPstmt bs = stmtMap.get(stmtKey);
    if (bs == null) {
      // the PreparedStatement has need been created
      return null;
    }
    // add the post execute processing for this bean/row
    bs.add(postExecute);

    // maintain a max batch size for any given batched stmt.
    // Used to determine when to flush.
    int bsSize = bs.size();
    if (bsSize > maxSize) {
      maxSize = bsSize;
    }
    return bs;
  }

  /**
   * Add a new PreparedStatement wrapped in the BatchStatement object.
   */
  public void addStmt(BatchedPstmt bs, BatchPostExecute postExecute) {
    // add the batch post execute to the statement for POST processing
    bs.add(postExecute);

    // cache so that getStmt() can find it for additional beans/rows
    stmtMap.put(bs.getSql(), bs);
  }

  /**
   * Return true if the batch has no statements to execute.
   */
  public boolean isEmpty() {
    return stmtMap.isEmpty();
  }

  /**
   * Execute one of the batched statements returning the row counts.
   */
  public int[] execute(String key, boolean getGeneratedKeys) throws SQLException {

    BatchedPstmt batchedPstmt = stmtMap.remove(key);
    if (batchedPstmt == null) {
      throw new PersistenceException("No batched statement found for key " + key);
    }
    batchedPstmt.executeBatch(getGeneratedKeys);
    return batchedPstmt.getResults();
  }

  /**
   * Execute all batched PreparedStatements.
   *
   * @param getGeneratedKeys if true try to get generated keys for inserts
   */
  public void flush(boolean getGeneratedKeys) throws BatchedSqlException {

    SQLException firstError = null;
    String errorSql = null;

    // flag set if something fails. Will not execute
    // but still need to close PreparedStatements.
    boolean isError = false;

    // if there are Listeners/Controllers that interact with the database,
    // the flush may get called recursively in executeBatch/postExecute.
    // which leads that we process stmtMap.values() twice in the loop.
    // So we copy the values, that we want to flush and clear it immediately.
    BatchedPstmt[] values = stmtMap.values().toArray(new BatchedPstmt[stmtMap.values().size()]);
    clear();

    // this loop
    for (BatchedPstmt bs : values) {
      try {
        if (!isError) {
          bs.executeBatch(getGeneratedKeys);
        }
      } catch (SQLException ex) {
        SQLException next = ex.getNextException();
        while (next != null) {
          logger.trace("Next Exception during batch execution", next);
          next = next.getNextException();
        }

        if (firstError == null) {
          firstError = ex;
          errorSql = bs.getSql();
        } else {
          logger.error("Error executing batched PreparedStatement", ex);
        }
        isError = true;

      } finally {
        try {
          bs.close();
        } catch (SQLException ex) {
          logger.error("Error closing batched PreparedStatement", ex);
        }
      }
    }


    if (firstError != null) {
      String msg = "Error when batch flush on sql: " + errorSql;
      throw new BatchedSqlException(msg, firstError);
    }
  }

  public void clear() {
    stmtMap.clear();
    maxSize = 0;
  }

  /**
   * Return the size of the biggest batched statement.
   * <p>
   * Used to determine when to flush the batch.
   * </p>
   */
  public int getMaxSize() {
    return maxSize;
  }

}
