package org.tests.model.basic.xtra;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestInsertBatchThenFlushThenUpdate extends BaseTestCase {

  @Test
  @IgnorePlatform(Platform.HANA)
  public void test() {

    LoggedSqlCollector.start();
    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);

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
      assertThat(LoggedSqlCollector.start()).isEmpty();

      txn.flushBatch();

      List<String> loggedSql1 = LoggedSqlCollector.start();
      assertThat(loggedSql1).hasSize(4);

      parent.setName("MyDesk");
      Ebean.save(parent);

      // nothing flushed yet
      assertThat(LoggedSqlCollector.start()).isEmpty();

      Ebean.commitTransaction();

      // insert statements for EdExtendedParent
      List<String> loggedSql2 = LoggedSqlCollector.start();
      assertThat(loggedSql2).hasSize(2);
      assertThat(loggedSql2.get(0)).contains(" update td_parent ");
    }
  }


}
