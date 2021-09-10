package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSqlUpdateBindMultipleLists extends BaseTestCase {

  @Test
  public void test() {

    LoggedSql.start();

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (:ids)");

    sqlUpdate.setParameter("ids", asList(9991, 9992, 9993));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?,?)", sqlUpdate.getGeneratedSql());

    // 3 parameters in the IN clause
    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?)", sqlUpdate.getGeneratedSql());

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
  }

  @Test
  public void positionParamsExpansion() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (?1)");

    sqlUpdate.setParameter(1, asList(9991, 9992, 9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?,?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (?1)");

    sqlUpdate.setParameter(1, asList(9991, 9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (?1)");

    sqlUpdate.setParameter(1, asList(9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?)", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPrePost() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id > ? and id in (?2) and id < ?")
      .setParameter(1, 90)
      .setParameter(2, asList(9991, 9992, 9993))
      .setParameter(3, 91);

    sqlUpdate.execute();
    assertEquals("delete from o_customer where id > ? and id in (?,?,?) and id < ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPrePost_2() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id > ? and id in (?2) and id < ?")
      .setParameter(90)
      .setParameter(asList(9991, 9992, 9993))
      .setParameter(91);

    sqlUpdate.execute();
    assertEquals("delete from o_customer where id > ? and id in (?,?,?) and id < ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPrePost_usingParam() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id > ? and id in (?2) and id < ?")
      .setParameters(90, asList(9991, 9992, 9993), 91);

    sqlUpdate.execute();
    assertEquals("delete from o_customer where id > ? and id in (?,?,?) and id < ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPre() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where name = ? and id in (?2)");

    sqlUpdate.setParameter(1, "Foo");
    sqlUpdate.setParameter(2, asList(9991, 9992, 9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where name = ? and id in (?,?,?)", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_multi() {

    SqlUpdate upd = DB.sqlUpdate("delete from o_customer where id in (?1) and name in (?2) and id not in (?3)");

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      upd.setParameters(asList(9991), asList("Foo"), asList(9994));
      upd.execute();

      upd.setParameters(asList(9991, 9992), asList("Foo", "Bar"), asList(1,2,3,4));
      upd.execute();

      upd.setParameters(asList(9991), asList("Foo", "Bar", "Baz"), asList(1,2));
      upd.execute();

      transaction.commit();
    }
  }

  @Test
  public void positionParamsExpansion_withPost() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (?1) and name = ?");

    sqlUpdate.setParameter(1, asList(9991, 9992, 9993));
    sqlUpdate.setParameter(2, "Foo");
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?,?,?) and name = ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPost_andBatch_execute() {

    SqlUpdate upd = DB.sqlUpdate("delete from o_customer where id in (?1) and name = ?");

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      upd.setParameters(asList(9991, 9992, 9993), "Foo");
      upd.execute();

      upd.setParameters(asList(9991, 9992), "Bar");
      upd.execute();

      upd.setParameters(asList(9991, 9992, 9999), "Baz");
      upd.execute();

      transaction.commit();
    }
  }

  @Test
  public void positionParamsExpansion_withPost_addBatch_execute() {

    SqlUpdate upd = DB.sqlUpdate("delete from o_customer where id in (?1) and name = ?");

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      upd.setParameters(asList(9991, 9992, 9993), "Foo");
      upd.addBatch();

      upd.setParameters(asList(9991, 9992), "Bar");
      upd.addBatch();

      upd.setParameters(asList(9991, 9992, 9999), "Baz");
      upd.addBatch();

      transaction.commit();
    }
  }

  @Test
  public void test_multipleLists_asPositioned() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (?1) and name in (?2)");

    try (Transaction transaction = DB.beginTransaction()) {

      sqlUpdate.setParameter(1, asList(9991, 9992, 9993));
      sqlUpdate.setParameter(2, asList("rob", "jim"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameters(asList(9991, 9992), asList("rob", "jim", "sd"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?) and name in (?,?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameter(1, asList(9991, 9992));
      sqlUpdate.setParameter(2, asList("rob", "jim"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameters(asList(9991), asList("rob", "jim"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());


      sqlUpdate.setParameters(asList(9992), asList("ro3b", "j3im"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameter(1, asList(9992, 4545));
      sqlUpdate.setParameter(2, asList("ro3b"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?) and name in (?)", sqlUpdate.getGeneratedSql());

      transaction.commit();
    }
  }

  @Test
  public void test_multipleLists_asPositioned_withBatch() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (?1) and name in (?2)");

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      sqlUpdate.setParameter(1, asList(9991, 9992, 9993));
      sqlUpdate.setParameter(2, asList("rob", "jim"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameters(asList(9991, 9992), asList("rob", "jim", "sd"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?) and name in (?,?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameter(1, asList(9991, 9992));
      sqlUpdate.setParameter(2, asList("rob", "jim"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameters(asList(9991), asList("rob", "jim"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());


      sqlUpdate.setParameters(asList(9992), asList("ro3b", "j3im"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());

      sqlUpdate.setParameter(1, asList(9992, 4545));
      sqlUpdate.setParameter(2, asList("ro3b"));
      sqlUpdate.execute();
      assertEquals("delete from o_customer where id in (?,?) and name in (?)", sqlUpdate.getGeneratedSql());

      transaction.commit();
    }
  }

  @Test
  public void test_multipleLists() {

    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from o_customer where id in (:ids) and name in (:names)");

    sqlUpdate.setParameter("ids", asList(9991, 9992, 9993));
    sqlUpdate.setParameter("names", asList("rob", "jim"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.setParameter("names", asList("rob", "jim", "sd"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?) and name in (?,?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.setParameter("names", asList("rob", "jim"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9991));
    sqlUpdate.setParameter("names", asList("rob", "jim"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9992));
    sqlUpdate.setParameter("names", asList("ro3b", "j3im"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9992, 4545));
    sqlUpdate.setParameter("names", asList("ro3b"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?) and name in (?)", sqlUpdate.getGeneratedSql());

  }

}
