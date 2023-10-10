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

public class TestInsertBatchThenUpdate extends BaseTestCase {

  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.HANA}) // has generated IDs
  public void test() {

    LoggedSql.start();
    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);

      LoggedSql.start();

      EdExtendedParent parent = new EdExtendedParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      DB.save(parent);

      // get Id or any generated property and that invokes a flush
      parent.getId();

      // going to get an update now
      parent.setName("MyDesk");
      DB.save(parent);

      txn.commit();

      // insert statements for EdExtendedParent
      List<String> loggedSql = LoggedSql.stop();
      assertThat(loggedSql).hasSize(9);
      assertThat(loggedSql.get(0)).contains("insert into td_parent");
      assertThat(loggedSql.get(3)).contains("insert into td_child ");
      assertThat(loggedSql.get(6)).contains("update td_parent set parent_name=? where parent_id=?");
    }
  }


  @Test
  @IgnorePlatform(Platform.HANA)
  public void test_noFlushOn_getterOfNonGeneratedProperty() {

    LoggedSql.start();
    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);

      LoggedSql.start();

      EdExtendedParent parent = new EdExtendedParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      DB.save(parent);

      // no getter call on a generated property
      // so no flush here
      String existing = parent.getName();

      parent.setName(existing+" - additional");
      // the first and second save of parent merge into a single insert
      DB.save(parent);

      // flush
      txn.commit();

      // insert statements for EdExtendedParent
      List<String> loggedSql = LoggedSql.stop();
      assertThat(loggedSql).hasSize(6);
      assertThat(loggedSql.get(0)).contains("insert into td_parent");
      assertThat(loggedSql.get(3)).contains("insert into td_child ");
    }
  }

}
