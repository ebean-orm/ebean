package com.avaje.ebean;

/**
 * Provides a callback that can be registered with a Transaction.
 * <p/>
 * The callback methods are called just prior to and after the transaction performs a commit or rollback.
 * <p/>
 * A typical use of TransactionCallback would be to clean up non-transactional resources like files. For example,
 * when processing files on postCommit/postRollback clean up the associated files. As another example when
 * on postCommit of a delete remove associated resources from the file system or remote service.
 */
public interface TransactionCallback {

  /**
   * Perform processing just prior to the transaction commit.
   */
  void preCommit();

  /**
   * Perform processing just after the transaction commit.
   */
  void postCommit();

  /**
   * Perform processing just prior to the transaction rollback.
   */
  void preRollback();

  /**
   * Perform processing just after the transaction rollback.
   */
  void postRollback();

}
