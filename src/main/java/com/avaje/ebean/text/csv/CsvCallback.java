package com.avaje.ebean.text.csv;

import com.avaje.ebean.EbeanServer;

/**
 * Provides callback methods for customisation of CSV processing.
 * <p>
 * You can provide your own CsvCallback implementation to customise the CSV
 * processing. It is expected that the DefaultCsvCallback provides a good base
 * class that you can extend.
 * </p>
 * 
 * @author rbygrave
 * 
 * @param <T>
 */
public interface CsvCallback<T> {

  /**
   * The processing is about to begin.
   * <p>
   * Typically the callback will create a transaction, set batch mode, batch
   * size etc.
   * </p>
   */
  void begin(EbeanServer server);

  /**
   * Read the header row.
   * <p>
   * This is only called if {@link CsvReader#setHasHeader(boolean,boolean)} has
   * been set to true.
   * </p>
   * 
   * @param line
   *          the header line content.
   */
  void readHeader(String[] line);

  /**
   * Check that the row should be processed - return true to process the row or
   * false to ignore the row. Gives ability to handle bad data... empty rows etc
   * and ignore it rather than fail.
   */
  boolean processLine(int row, String[] line);

  /**
   * Called for each bean after it has been loaded from the CSV content.
   * <p>
   * This allows you to process the bean however you like.
   * </p>
   * <p>
   * When you use a CsvCallback the CsvReader *WILL NOT* create a transaction
   * and will not save the bean for you. You have complete control and must do
   * these things yourself (if that is want you want).
   * </p>
   * 
   * @param row
   *          the index of the content being processed
   * @param line
   *          the content that has been used to load the bean
   * @param bean
   *          the entity bean after it has been loaded from the csv content
   */
  void processBean(int row, String[] line, T bean);

  /**
   * The processing has ended successfully.
   * <p>
   * Typically the callback will commit the transaction.
   * </p>
   */
  void end(int row);

  /**
   * The processing has ended due to an error.
   * <p>
   * This gives the callback the opportunity to rollback the transaction if one
   * was created.
   * </p>
   * 
   * @param row
   *          the row that the error has occured on
   * @param e
   *          the error that occured
   */
  void endWithError(int row, Exception e);

}
