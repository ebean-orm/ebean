package org.tests.update;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUpdateAllLoadedProperties extends BaseTestCase {

  @Test
  public void test() {

    EBasicVer basic1 = new EBasicVer("basic1");
    basic1.setDescription("aaa");
    DB.save(basic1);

    EBasicVer basic2 = new EBasicVer("basic2");
    basic1.setDescription("bbb");
    DB.save(basic2);


    Database server = DB.getDefault();
    try (Transaction txn = server.beginTransaction()) {

      txn.setUpdateAllLoadedProperties(true);

      LoggedSql.start();

      basic2.setDescription("bbb-mod");
      server.save(basic2, txn);

      basic1.setName("basic1-mod");
      basic1.setDescription("aaa-mod");
      server.save(basic1, txn);

      txn.commit();

      List<String> loggedSql = LoggedSql.stop();

      assertEquals(2, loggedSql.size());
      // all properties in the bean
      assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, description=?, other=?, last_update=? where id=? and last_update=?; -- bind(");
      assertThat(loggedSql.get(1)).contains("update e_basicver set name=?, description=?, other=?, last_update=? where id=? and last_update=?; -- bind(");
    }

    testPartiallyLoaded(basic1.getId(), basic2.getId());

  }

  private void testPartiallyLoaded(Integer id1, Integer id2) {

    List<Integer> ids = new ArrayList<>();
    ids.add(id1);
    ids.add(id2);

    List<EBasicVer> beans = DB.find(EBasicVer.class)
      .select("name, other")
      .where().idIn(ids)
      .orderBy().asc("id")
      .findList();

    assertEquals(2, beans.size());

    LoggedSql.start();


    Transaction txn = DB.beginTransaction();
    try {
      txn.setUpdateAllLoadedProperties(true);

      EBasicVer basic1 = beans.get(0);
      basic1.setName("jim");
      DB.save(basic1);

      EBasicVer basic2 = beans.get(1);
      basic2.setName("john");
      basic2.setOther("otherDesc");
      DB.save(basic2);

      txn.commit();

    } finally {
      txn.end();
    }

    List<String> loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(2);
    assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, other=? where id=?; -- bind(");
    assertThat(loggedSql.get(1)).contains("update e_basicver set name=?, other=? where id=?; -- bind(");

  }
}
