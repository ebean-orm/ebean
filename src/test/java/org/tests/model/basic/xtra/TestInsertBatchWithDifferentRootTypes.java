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

public class TestInsertBatchWithDifferentRootTypes extends BaseTestCase {

  @Test
  public void testDifferRootTypes() {

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

      EdExtendedParent extendedParent = new EdExtendedParent();
      extendedParent.setName("My second computer");
      extendedParent.setExtendedName("Multimedia");

      child = new EdChild();
      child.setName("DVBS Card");
      children = new ArrayList<>();
      children.add(child);
      extendedParent.setChildren(children);

      // nothing flushed yet
      List<String> loggedSql0 = LoggedSqlCollector.start();
      assertEquals(0, loggedSql0.size());

      // does not causes a flush as EdExtendedParent is same root as EdParent
      // so they get the same batch depth
      Ebean.save(extendedParent);

      // insert statements for EdParent
      List<String> loggedSql1 = LoggedSqlCollector.start();

      Ebean.commitTransaction();

      assertEquals(0, loggedSql1.size());

      // insert statements for EdExtendedParent
      List<String> loggedSql2 = LoggedSqlCollector.start();
      assertEquals(4, loggedSql2.size());

    } finally {
      Ebean.endTransaction();
    }
  }


}
