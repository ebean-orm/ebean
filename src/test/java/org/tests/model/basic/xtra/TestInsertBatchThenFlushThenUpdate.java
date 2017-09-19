package org.tests.model.basic.xtra;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestInsertBatchThenFlushThenUpdate extends BaseTestCase {

  @Test
  public void test() {

    LoggedSqlCollector.start();
    Transaction txn = Ebean.beginTransaction();
    try {
      txn.setBatch(PersistBatch.ALL);

      EdParent parent = new EdParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      Ebean.save(parent);

      // nothing flushed yet
      assertEquals(0, LoggedSqlCollector.start().size());

      txn.flushBatch();

      List<String> loggedSql1 = LoggedSqlCollector.start();
      assertEquals(loggedSql1.toString(), 2, loggedSql1.size());

      parent.setName("MyDesk");
      Ebean.save(parent);

      // nothing flushed yet
      assertEquals(0, LoggedSqlCollector.start().size());

      Ebean.commitTransaction();

      // insert statements for EdExtendedParent
      List<String> loggedSql2 = LoggedSqlCollector.start();
      assertEquals(1, loggedSql2.size());
      assertTrue(loggedSql2.get(0).contains(" update td_parent "));

    } finally {
      Ebean.endTransaction();
    }
  }


}
