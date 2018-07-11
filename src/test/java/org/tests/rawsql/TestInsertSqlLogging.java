package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.Test;
import org.tests.idkeys.db.AuditLog;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;

public class TestInsertSqlLogging extends BaseTestCase {

  @Test
  public void test() {

    Ebean.find(AuditLog.class).where().ge("id", 10000).delete();

    String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
    SqlUpdate insert = Ebean.createSqlUpdate(sql);

    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);
      txn.setBatchSize(2);

      insert.setNextParameter(10000);
      insert.setNextParameter("hello");
      insert.setNextParameter("rob");
      insert.execute();

      insert.setNextParameter(10001);
      insert.setNextParameter("goodbye");
      insert.setNextParameter("rob");
      insert.execute();

      insert.setNextParameter(10002);
      insert.setNextParameter("chow");
      insert.setNextParameter("bob");
      insert.execute();

      txn.commit();
    }
  }

  @Test
  public void addBatch_executeBatch() {

    Ebean.find(AuditLog.class).where().ge("id", 10000).delete();

    String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
    SqlUpdate insert = Ebean.createSqlUpdate(sql);

    try (Transaction txn = Ebean.beginTransaction()) {

      insert.setNextParameter(10000);
      insert.setNextParameter("hello");
      insert.setNextParameter("rob");
      insert.addBatch();

      insert.setNextParameter(10001);
      insert.setNextParameter("goodbye");
      insert.setNextParameter("rob");
      insert.addBatch();

      insert.setNextParameter(10002);
      insert.setNextParameter("chow");
      insert.setNextParameter("bob");
      insert.addBatch();

      int[] rows = insert.executeBatch();
      System.out.println("Rows was " + Arrays.toString(rows));

      txn.commit();
    }

  }

  @Test
  public void test_trim_leadingSpaces_eventFound() {

    Transaction transaction = Ebean.beginTransaction();
    try {
      String upd = "   update audit_log set description='junk' where id = 10001";
      SqlUpdate update = Ebean.createSqlUpdate(upd);
      update.execute();
      transaction.commit();

      assertFalse(((SpiTransaction) transaction).getEvent().getEventTables().isEmpty());

    } finally {
      transaction.end();
    }
  }
}
