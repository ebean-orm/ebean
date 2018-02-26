package io.ebeaninternal.server.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Document store only transaction.
 */
public class DocStoreOnlyTransaction extends JdbcTransaction {

  /**
   * Create a new DocStore  only Transaction.
   */
  public DocStoreOnlyTransaction(String id, boolean explicit, TransactionManager manager) {
    super(id, explicit, null, manager);
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
  public Connection getInternalConnection() {
    throw new RuntimeException("not supported on DocStoreTransaction");
  }

  @Override
  public Connection getConnection() {
    throw new RuntimeException("not supported on DocStoreTransaction");
  }

  @Override
  protected void performRollback() throws SQLException {
    // do nothing (could perhaps throw not supported exception)
  }

  @Override
  protected void performCommit() throws SQLException {
    if (docStoreTxn != null) {
      manager.docStoreUpdateProcessor.commit(docStoreTxn);
    }
  }

}
