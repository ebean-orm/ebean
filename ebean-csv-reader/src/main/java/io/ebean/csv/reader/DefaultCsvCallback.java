package io.ebean.csv.reader;

import io.ebean.Database;
import io.ebean.EbeanVersion;
import io.ebean.Transaction;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

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
 * @param <T>
 */
public class DefaultCsvCallback<T> implements CsvCallback<T> {

  private static final System.Logger log = EbeanVersion.log;

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
  protected Database server;

  /**
   * Used to log a message to indicate progress through large files.
   */
  protected final int logInfoFrequency;

  /**
   * The batch size used when saving the beans.
   */
  protected final int persistBatchSize;

  protected boolean getGeneratedKeys = true;
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
  @Override
  public void begin(Database server) {
    this.server = server;
    this.startTime = System.currentTimeMillis();
    initTransactionIfRequired();
  }

  /**
   * Override to read the heading line.
   * <p>
   * This is only called if {@link CsvReader#setHasHeader(boolean, boolean)} is
   * set to true.
   * <p>
   * By default this does nothing (effectively ignoring the heading).
   */
  @Override
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
  @Override
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
  @Override
  public void processBean(int row, String[] line, T bean) {
    // assumes single bean or Cascade.PERSIST will save any
    // related beans (e.g. customer -> customer.billingAddress
    server.save(bean, transaction);
    if (logInfoFrequency > 0 && (row % logInfoFrequency == 0)) {
      log.log(DEBUG, "processed {0} rows", row);
    }
  }

  /**
   * Commit the transaction if one was created.
   */
  @Override
  public void end(int row) {
    commitTransactionIfCreated();
    exeTime = System.currentTimeMillis() - startTime;
    log.log(INFO, "Csv finished, rows[{0}] exeMillis[{1}]", row, exeTime);
  }

  /**
   * Rollback the transaction if one was created.
   */
  @Override
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
        transaction.setBatchMode(true);
        transaction.setBatchSize(persistBatchSize);
        transaction.setGetGeneratedKeys(getGeneratedKeys);
      } else {
        // explicitly turn off JDBC batching in case
        // is has been turned on globally
        transaction.setBatchMode(false);
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
    }
  }

  /**
   * Rollback the transaction if we where not successful in processing all the
   * rows.
   */
  protected void rollbackTransactionIfCreated(Throwable e) {
    if (createdTransaction) {
      transaction.rollback(e);
    }
  }

}
