package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ESimple;

/**
 * This test tests a strange bug in the 6.2.0. sqlserver JDBC driver.
 * (Version 6.1.7.jre8-preview works)
 * <p>
 * https://github.com/Microsoft/mssql-jdbc/pull/374
 *
 * @author Roland Praml, FOCONIS AG
 */
public class TestSqlServerBatch extends BaseTestCase {

  @IgnorePlatform(value = Platform.SQLSERVER)
  @Test
  public void testBasicIdentityBatch() {

    Transaction txn = DB.beginTransaction();
    try {
      txn.setBatchMode(true);
      txn.setBatchSize(3);

      for (int i = 0; i < 10; i++) {
        ESimple model = new ESimple();
        model.setName("baz " + i);
        DB.save(model);
      }

      txn.commit();

    } finally {
      txn.end();
    }
  }

  @Test
  public void testAggressiveBatch() {

    Transaction txn = DB.beginTransaction();
    try {
      txn.setBatchMode(true);
      txn.setBatchSize(3);

      // control flushing when mixing save and queries
      txn.setFlushOnQuery(false);

      // for large batch insert processing when we do not
      // ... need the generatedKeys, don't get them
      txn.setGetGeneratedKeys(false);

      // explicitly flush the JDBC batch buffer
      txn.flush();

      for (int i = 0; i < 10; i++) {
        ESimple model = new ESimple();
        model.setName(i % 2 == 0 ? null : "foobar");
        DB.save(model);
      }

      // do not commit
    } finally {
      txn.end();
    }
  }
}
