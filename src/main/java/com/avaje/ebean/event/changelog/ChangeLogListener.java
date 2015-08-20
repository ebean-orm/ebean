package com.avaje.ebean.event.changelog;

/**
 * Listen for changes.
 * <p>
 * Implementations can take the changes and store them in a document store for auditing purposes etc.
 * </p>
 */
public interface ChangeLogListener {

  /**
   * Log the batch of changes.
   * <p>
   * For small transactions this will be all the changes in the transaction.
   * For larger/longer transactions this can be a 'batch' of changes made and the actual transaction
   * has not yet committed or rolled back and a later change set will contain the final changeSet for
   * the transaction with it's final status of <code>COMMITTED</code> or <code>ROLLBACK</code>.
   */
  void log(ChangeSet changeSet);

}
