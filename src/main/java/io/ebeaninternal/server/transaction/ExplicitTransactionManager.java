package io.ebeaninternal.server.transaction;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.api.SpiTransaction;

import java.sql.Connection;

/**
 * TransactionManager where the transactions start with explicit "begin" statement.
 */
public class ExplicitTransactionManager extends TransactionManager {

  public ExplicitTransactionManager(TransactionManagerOptions options) {
    super(options);
  }

  /**
   * Create a ExplicitJdbcTransaction.
   */
  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {

    return new ExplicitJdbcTransaction(prefix + id, explicit, c, this);
  }

  /**
   * Override the initialise of OnQueryOnly with the intention not to use CLOSE with ExplicitJdbcTransaction.
   */
  @Override
  protected DatabasePlatform.OnQueryOnly initOnQueryOnly(DatabasePlatform.OnQueryOnly dbPlatformOnQueryOnly) {

    // first check for a system property 'override'
    String systemPropertyValue = System.getProperty("ebean.transaction.onqueryonly");
    if (systemPropertyValue != null) {
      return DatabasePlatform.OnQueryOnly.valueOf(systemPropertyValue.trim().toUpperCase());
    }

    // default to rollback if not defined on the platform
    return dbPlatformOnQueryOnly == null ? DatabasePlatform.OnQueryOnly.ROLLBACK : dbPlatformOnQueryOnly;
  }
}
