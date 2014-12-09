package com.avaje.tests.model.basic.xtra;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.PersistBatch;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestInsertBatchThenUpdate extends BaseTestCase {

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
      ArrayList<EdChild> children = new ArrayList<EdChild>();
      children.add(child);
      parent.setChildren(children);

      Ebean.save(parent);

      // nothing flushed yet
      List<String> loggedSql0 = LoggedSqlCollector.start();
      assertEquals(0, loggedSql0.size());

      parent.setName("MyDesk");
      Ebean.save(parent);

      // nothing flushed yet
      assertEquals(0, LoggedSqlCollector.start().size());

      Ebean.commitTransaction();

      // insert statements for EdExtendedParent
      List<String> loggedSql2 = LoggedSqlCollector.start();
      assertEquals(2, loggedSql2.size());

    } finally {
      Ebean.endTransaction();
    }
  }


}
