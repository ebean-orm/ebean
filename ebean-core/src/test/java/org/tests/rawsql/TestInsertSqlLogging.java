package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.DB;
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

    DB.find(AuditLog.class).where().ge("id", 10000).delete();

    String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
    SqlUpdate insert = DB.sqlUpdate(sql);

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);
      txn.setBatchSize(2);

      insert.setParameter(10000);
      insert.setParameter("hello");
      insert.setParameter("rob");
      insert.execute();

      insert.setParameter(10001);
      insert.setParameter("goodbye");
      insert.setParameter("rob");
      insert.execute();

      insert.setParameter(10002);
      insert.setParameter("chow");
      insert.setParameter("bob");
      insert.execute();

      txn.commit();
    }
  }

  @Test
  public void addBatch_executeBatch() {

    DB.find(AuditLog.class).where().ge("id", 10000).delete();

    String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
    SqlUpdate insert = DB.sqlUpdate(sql);

    try (Transaction txn = DB.beginTransaction()) {

      insert.setParameter(10000);
      insert.setParameter("hello");
      insert.setParameter("rob");
      insert.addBatch();

      insert.setParameter(10001);
      insert.setParameter("goodbye");
      insert.setParameter("rob");
      insert.addBatch();

      insert.setParameter(10002);
      insert.setParameter("chow");
      insert.setParameter("bob");
      insert.addBatch();

      int[] rows = insert.executeBatch();
      System.out.println("Rows was " + Arrays.toString(rows));

      txn.commit();
    }
  }

  @Test
  public void addBatch_namedParams() {

    DB.find(AuditLog.class).where().ge("id", 10000).delete();

    String sql = "insert into audit_log (id, description, modified_description) values (:id, :desc, :modDesc)";
    SqlUpdate insert = DB.sqlUpdate(sql);

    try (Transaction txn = DB.beginTransaction()) {

      insert.setParameter("id", 20000);
      insert.setParameter("desc", "hello");
      insert.setParameter("modDesc", "rob");
      insert.addBatch();

      insert.setParameter("id", 20001);
      insert.setParameter("desc", "goodbye");
      insert.setParameter("modDesc", "rob");
      insert.addBatch();

      insert.setParameter("id", 20002);
      insert.setParameter("desc", "chow");
      insert.setParameter("modDesc", "bob");
      insert.addBatch();

      int[] rows = insert.executeBatch();
      System.out.println("Rows was " + Arrays.toString(rows));

      txn.commit();
    }
  }

  @Test
  public void test_trim_leadingSpaces_eventFound() {

    Transaction transaction = DB.beginTransaction();
    try {
      String upd = "   update audit_log set description='junk' where id = 10001";
      SqlUpdate update = DB.sqlUpdate(upd);
      update.execute();
      transaction.commit();

      assertFalse(((SpiTransaction) transaction).getEvent().getEventTables().isEmpty());

    } finally {
      transaction.end();
    }
  }
}
