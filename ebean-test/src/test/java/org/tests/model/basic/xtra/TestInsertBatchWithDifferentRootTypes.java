package org.tests.model.basic.xtra;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInsertBatchWithDifferentRootTypes extends BaseTestCase {

  @Test
  @IgnorePlatform(Platform.HANA)
  public void testDifferRootTypes() {

    LoggedSql.start();

    try (Transaction txn = DB.beginTransaction())  {
      txn.setBatchMode(true);

      EdExtendedParent parent = new EdExtendedParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      DB.save(parent);

      EdExtendedParent extendedParent = new EdExtendedParent();
      extendedParent.setName("My second computer");
      extendedParent.setExtendedName("Multimedia");

      child = new EdChild();
      child.setName("DVBS Card");
      children = new ArrayList<>();
      children.add(child);
      extendedParent.setChildren(children);

      // nothing flushed yet
      List<String> loggedSql0 = LoggedSql.start();
      assertEquals(0, loggedSql0.size());

      // does not causes a flush as EdExtendedParent is same root as EdParent
      // so they get the same batch depth
      DB.save(extendedParent);

      // insert statements for EdParent
      List<String> loggedSql1 = LoggedSql.start();

      txn.commit();

      assertEquals(0, loggedSql1.size());

      // insert statements for EdExtendedParent
      List<String> loggedSql2 = LoggedSql.start();
      assertThat(loggedSql2).hasSize(8);
    }
  }


}
