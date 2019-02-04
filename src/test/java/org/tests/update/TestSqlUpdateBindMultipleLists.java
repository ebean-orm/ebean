package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TestSqlUpdateBindMultipleLists extends BaseTestCase {

  @Test
  public void test() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (:ids)");

    sqlUpdate.setParameter("ids", asList(9991, 9992, 9993));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?,?)", sqlUpdate.getGeneratedSql());

    // 3 parameters in the IN clause
    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?)", sqlUpdate.getGeneratedSql());

  }

  @Test
  public void positionParamsExpansion() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (?1)");

    sqlUpdate.setParameter(1, asList(9991, 9992, 9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?,?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (?1)");

    sqlUpdate.setParameter(1, asList(9991, 9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (?1)");

    sqlUpdate.setParameter(1, asList(9993));
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?)", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPrePost() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id > ? and id in (?2) and id < ?");

    sqlUpdate.setParameter(1, 90);
    sqlUpdate.setParameter(2, asList(9991, 9992, 9993));
    sqlUpdate.setParameter(3, 91);

    sqlUpdate.execute();

    assertEquals("delete from o_customer where id > ? and id in (?,?,?) and id < ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPrePost_usingParam() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id > ? and id in (?2) and id < ?");

    sqlUpdate.setParams(90, asList(9991, 9992, 9993), 91);
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id > ? and id in (?,?,?) and id < ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPre() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where name = ? and id in (?2)");

    sqlUpdate.setParameter(1, "Foo");
    sqlUpdate.setParameter(2, asList(9991, 9992, 9993));

    sqlUpdate.execute();

    assertEquals("delete from o_customer where name = ? and id in (?,?,?)", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPost() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (?1) and name = ?");

    sqlUpdate.setParameter(1, asList(9991, 9992, 9993));
    sqlUpdate.setParameter(2, "Foo");
    sqlUpdate.execute();

    assertEquals("delete from o_customer where id in (?,?,?) and name = ?", sqlUpdate.getGeneratedSql());
  }

  @Test
  public void positionParamsExpansion_withPost_andBatch() {

    SqlUpdate upd = Ebean.createSqlUpdate("delete from o_customer where id in (:ids) and name = :name");

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      upd.setParameter("ids", asList(9991, 9992, 9993));
      upd.setParameter("name", "Foo");
      upd.execute();

      upd.setParameter("ids", asList(9991, 9992));
      upd.setParameter("name", "Bar");
      upd.execute();

      upd.setParameter("ids", asList(9991, 9992, 9999));
      upd.setParameter("name", "Baz");
      upd.execute();

      transaction.commit();
    }
  }

  @Test
  public void test_multipleLists() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (:ids) and name in (:names)");

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
