package com.avaje.ebean.text.csv;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the default implementation of CsvCallback.
 * <p>
 * This handles transaction creation (if no current transaction existed) and
 * transaction commit or rollback on error.
 * </p>
 * <p>
 * For customising the processing you can extend this object and override the
 * appropriate methods.
 * </p>
 * 
 * @author rob
 * 
 * @param <T>
 */
public class DefaultCsvCallback<T> implements CsvCallback<T> {

  private static final Logger logger = LoggerFactory.getLogger(DefaultCsvCallback.class);

  /**
   * The transaction to use (if not using CsvCallback).
   */
  protected Transaction transaction;

  /**
   * Flag set when we created the transaction.
   */
  protected boolean createdTransaction;

  /**
   * The EbeanServer used to save the beans.
   */
  protected EbeanServer server;

  /**
   * Used to log a message to indicate progress through large files.
   */
  protected final int logInfoFrequency;

  /**
   * The batch size used when saving the beans.
   */
  protected final int persistBatchSize;

  /**
   * The time the process started.
   */
  protected long startTime;

  /**
   * The execution time of the process.
   */
  protected long exeTime;

  /**
   * Construct with a default batch size of 30 and logging info messages every
   * 1000 rows.
   */
  public DefaultCsvCallback() {
    this(30, 1000);
  }

  /**
   * Construct with explicit batch size and logging info frequency.
   */
  public DefaultCsvCallback(int persistBatchSize, int logInfoFrequency) {

    this.persistBatchSize = persistBatchSize;
    this.logInfoFrequency = logInfoFrequency;
  }

  /**
   * Create a transaction if required.
   */
  public void begin(EbeanServer server) {
    this.server = server;
    this.startTime = System.currentTimeMillis();

    initTransactionIfRequired();
  }

  /**
   * Override to read the heading line.
   * <p>
   * This is only called if {@link CsvReader#setHasHeader(boolean,boolean)} is
   * set to true.
   * </p>
   * <p>
   * By default this does nothing (effectively ignoring the heading).
   * </p>
   */
  public void readHeader(String[] line) {

  }

  /**
   * Validate that the content is valid and return false if the row should be
   * ignored.
   * <p>
   * By default this just returns true.
   * </p>
   * <p>
   * Override this to add custom validation logic returning false if you want
   * the row to be ignored. For example, if all the content is empty return
   * false to ignore the row (rather than having the processing fail with some
   * error).
   * </p>
   */
  public boolean processLine(int row, String[] line) {
    return true;
  }

  /**
   * Will save the bean.
   * <p>
   * Override this method to customise the bean (set additional properties etc)
   * or to control the saving of other related beans (when you can't/don't want
   * to use Cascade.PERSIST etc).
   * </p>
   */
  public void processBean(int row, String[] line, T bean) {

    // assumes single bean or Cascade.PERSIST will save any
    // related beans (e.g. customer -> customer.billingAddress
    server.save(bean, transaction);

    if (logInfoFrequency > 0 && (row % logInfoFrequency == 0)) {
      logger.info("processed " + row + " rows");
    }
  }

  /**
   * Commit the transaction if one was created.
   */
  public void end(int row) {

    commitTransactionIfCreated();

    exeTime = System.currentTimeMillis() - startTime;
    logger.info("Csv finished, rows[" + row + "] exeMillis[" + exeTime + "]");
  }

  /**
   * Rollback the transaction if one was created.
   */
  public void endWithError(int row, Exception e) {
    rollbackTransactionIfCreated(e);
  }

  /**
   * Create a transaction if one is not already active and set its batch mode
   * and batch size.
   */
  protected void initTransactionIfRequired() {

    transaction = server.currentTransaction();
    if (transaction == null || !transaction.isActive()) {

      transaction = server.beginTransaction();
      createdTransaction = true;
      if (persistBatchSize > 1) {
        logger.info("Creating transaction, batchSize[" + persistBatchSize + "]");
        transaction.setBatchMode(true);
        transaction.setBatchSize(persistBatchSize);
        transaction.setBatchGetGeneratedKeys(false);

      } else {
        // explicitly turn off JDBC batching in case
        // is has been turned on globally
        transaction.setBatchMode(false);
        logger.info("Creating transaction with no JDBC batching");
      }
    }
  }

  /**
   * If we created a transaction commit it. We have successfully processed all
   * the rows.
   */
  protected void commitTransactionIfCreated() {
    if (createdTransaction) {
      transaction.commit();
      logger.info("Committed transaction");
    }
  }

  /**
   * Rollback the transaction if we where not successful in processing all the
   * rows.
   */
  protected void rollbackTransactionIfCreated(Throwable e) {
    if (createdTransaction) {
      transaction.rollback(e);
      logger.info("Rolled back transaction");
    }
  }

}
