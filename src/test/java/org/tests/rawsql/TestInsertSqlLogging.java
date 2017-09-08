package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiTransaction;
import org.tests.idkeys.db.AuditLog;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TestInsertSqlLogging extends BaseTestCase {

  @Test
  public void test() {

    Ebean.delete(AuditLog.class, 10000);

    String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
    sqlUpdate.setParameter(1, 10000);
    sqlUpdate.setParameter(2, "hello");
    sqlUpdate.setParameter(3, "rob");

    sqlUpdate.execute();
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
