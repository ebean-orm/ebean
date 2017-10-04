package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import org.tests.model.basic.EBasicVer;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestUpdateAllLoadedProperties extends BaseTestCase {

  @Test
  public void test() {

    EBasicVer basic1 = new EBasicVer("basic1");
    basic1.setDescription("aaa");
    Ebean.save(basic1);

    EBasicVer basic2 = new EBasicVer("basic2");
    basic1.setDescription("bbb");
    Ebean.save(basic2);


    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {

      txn.setUpdateAllLoadedProperties(true);

      LoggedSqlCollector.start();

      basic2.setDescription("bbb-mod");
      server.save(basic2, txn);

      basic1.setName("basic1-mod");
      basic1.setDescription("aaa-mod");
      server.save(basic1, txn);

      txn.commit();

      List<String> loggedSql = LoggedSqlCollector.stop();

      assertEquals(2, loggedSql.size());
      // all properties in the bean
      assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, description=?, other=?, last_update=? where id=? and last_update=?; --bind(");
      assertThat(loggedSql.get(1)).contains("update e_basicver set name=?, description=?, other=?, last_update=? where id=? and last_update=?; --bind(");

    } finally {
      txn.end();
    }

    testPartiallyLoaded(basic1.getId(), basic2.getId());

  }

  private void testPartiallyLoaded(Integer id1, Integer id2) {

    List<Integer> ids = new ArrayList<>();
    ids.add(id1);
    ids.add(id2);

    List<EBasicVer> beans = Ebean.find(EBasicVer.class)
      .select("name, other")
      .where().idIn(ids)
      .order().asc("id")
      .findList();

    assertEquals(2, beans.size());

    LoggedSqlCollector.start();


    Transaction txn = Ebean.beginTransaction();
    try {
      txn.setUpdateAllLoadedProperties(true);

      EBasicVer basic1 = beans.get(0);
      basic1.setName("jim");
      Ebean.save(basic1);

      EBasicVer basic2 = beans.get(1);
      basic2.setName("john");
      basic2.setOther("otherDesc");
      Ebean.save(basic2);

      txn.commit();

    } finally {
      txn.end();
    }

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(2);
    assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, other=? where id=?; --bind(");
    assertThat(loggedSql.get(1)).contains("update e_basicver set name=?, other=? where id=?; --bind(");

  }
}
