package io.ebeaninternal.server.transaction;

import java.sql.Connection;

/**
 * Document store only transaction.
 */
public final class DocStoreOnlyTransaction extends JdbcTransaction {

  /**
   * Create a new DocStore  only Transaction.
   */
  public DocStoreOnlyTransaction(boolean explicit, TransactionManager manager) {
    super(explicit, null, manager);
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    // do nothing
  }

  @Override
  protected void deactivate() {
    // do nothing
  }

  @Override
  public Connection internalConnection() {
    throw new RuntimeException("not supported on DocStoreTransaction");
  }

  @Override
  public Connection connection() {
    throw new RuntimeException("not supported on DocStoreTransaction");
  }

  @Override
  protected void performRollback() {
    // do nothing (could perhaps throw not supported exception)
  }

  @Override
  protected void performCommit() {
    if (docStoreTxn != null) {
      manager.docStoreUpdateProcessor.commit(docStoreTxn);
    }
  }

}
