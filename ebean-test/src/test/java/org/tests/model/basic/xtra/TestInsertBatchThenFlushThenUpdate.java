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

public class TestInsertBatchThenFlushThenUpdate extends BaseTestCase {

  @Test
  @IgnorePlatform(Platform.HANA)
  public void test() {

    LoggedSql.start();
    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);

      EdParent parent = new EdParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      DB.save(parent);

      // nothing flushed yet
      assertThat(LoggedSql.start()).isEmpty();

      txn.flush();

      List<String> loggedSql1 = LoggedSql.start();
      assertThat(loggedSql1).hasSize(6);

      parent.setName("MyDesk");
      DB.save(parent);

      // nothing flushed yet
      assertThat(LoggedSql.start()).isEmpty();

      txn.commit();

      // insert statements for EdExtendedParent
      List<String> loggedSql2 = LoggedSql.start();
      assertThat(loggedSql2).hasSize(3);
      assertThat(loggedSql2.get(0)).contains(" update td_parent ");
    }
  }


}
